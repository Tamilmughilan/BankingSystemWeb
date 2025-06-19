package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CookieSameSiteFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        boolean isSecure = httpRequest.isSecure();
        
       
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse) {
            @Override
            public void addCookie(Cookie cookie) {
                // Build cookie header with SameSite attribute
                StringBuilder cookieHeader = new StringBuilder();
                cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
                
                // Add path
                cookieHeader.append("; Path=").append(cookie.getPath() != null ? cookie.getPath() : "/");
                
                // Add HttpOnly if not already set
                if (cookie.isHttpOnly()) {
                    cookieHeader.append("; HttpOnly");
                }
                
                // Add Secure flag only for HTTPS
                if (isSecure && cookie.getSecure()) {
                    cookieHeader.append("; Secure");
                }
                
                // Add Max-Age if set
                if (cookie.getMaxAge() >= 0) {
                    cookieHeader.append("; Max-Age=").append(cookie.getMaxAge());
                }
                
                // Add SameSite attribute
                cookieHeader.append("; SameSite=Lax");
                
                // Use addHeader instead of setHeader to allow multiple cookies
                super.addHeader("Set-Cookie", cookieHeader.toString());
            }
        };
        
        // Process the request
        chain.doFilter(request, responseWrapper);
        

        Collection<String> cookieHeaders = httpResponse.getHeaders("Set-Cookie");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            // Clear existing headers
            httpResponse.setHeader("Set-Cookie", null);
            
            //add each cookie with SameSite attribute
            for (String cookieHeader : cookieHeaders) {
                String modifiedCookie = cookieHeader;
                
              
                if (!cookieHeader.toLowerCase().contains("samesite")) {
                    modifiedCookie += "; SameSite=Lax";
                }
                
       
                
                httpResponse.addHeader("Set-Cookie", modifiedCookie);
            }
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CookieSameSiteFilter initialized");
    }
    
    @Override
    public void destroy() {
 
    }

}