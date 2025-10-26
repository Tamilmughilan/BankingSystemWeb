package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Collection;

/**
 * Enhanced filter to add SameSite attribute to ALL cookies including JSESSIONID.
 * This addresses the "Cookie without SameSite Attribute" vulnerability.
 * 
 * @author TAMIL MUGHILAN
 */
@WebFilter("/*")
public class CookieSameSiteFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Use enhanced response wrapper
        EnhancedSameSiteWrapper responseWrapper = new EnhancedSameSiteWrapper(httpResponse, httpRequest);
        
        // Process request
        chain.doFilter(request, responseWrapper);
        
        // Final cleanup - catch any remaining cookies
        responseWrapper.processFinalCookies();
    }
    
    /**
     * Enhanced wrapper that catches ALL cookie operations
     */
    private static class EnhancedSameSiteWrapper extends HttpServletResponseWrapper {
        private final HttpServletRequest request;
        private final HttpServletResponse originalResponse;
        
        public EnhancedSameSiteWrapper(HttpServletResponse response, HttpServletRequest request) {
            super(response);
            this.originalResponse = response;
            this.request = request;
        }
        
        @Override
        public void addCookie(Cookie cookie) {
            // Create secure cookie header manually
            String cookieHeader = buildSecureCookieHeader(cookie);
            super.addHeader("Set-Cookie", cookieHeader);
        }
        
        @Override
        public void addHeader(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name)) {
                value = enhanceCookieHeader(value);
            }
            super.addHeader(name, value);
        }
        
        @Override
        public void setHeader(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name)) {
                value = enhanceCookieHeader(value);
            }
            super.setHeader(name, value);
        }
        
        /**
         * Builds a complete secure cookie header
         */
        private String buildSecureCookieHeader(Cookie cookie) {
            StringBuilder header = new StringBuilder();
            header.append(cookie.getName()).append("=").append(cookie.getValue());
            
            // Path
            if (cookie.getPath() != null) {
                header.append("; Path=").append(cookie.getPath());
            } else {
                header.append("; Path=/");
            }
            
            // Domain
            if (cookie.getDomain() != null) {
                header.append("; Domain=").append(cookie.getDomain());
            }
            
            // HttpOnly
            if (cookie.isHttpOnly()) {
                header.append("; HttpOnly");
            }
            
            // Secure (for HTTPS)
            if (cookie.getSecure() || request.isSecure()) {
                header.append("; Secure");
            }
            
            // Max-Age
            if (cookie.getMaxAge() >= 0) {
                header.append("; Max-Age=").append(cookie.getMaxAge());
            }
            
            // SameSite - Strict for session cookies, Lax for others
            if ("JSESSIONID".equals(cookie.getName())) {
                header.append("; SameSite=Strict");
            } else {
                header.append("; SameSite=Lax");
            }
            
            return header.toString();
        }
        
        /**
         * Enhances existing cookie headers with security attributes
         */
        private String enhanceCookieHeader(String cookieHeader) {
            if (cookieHeader == null) return null;
            
            String lowerCookie = cookieHeader.toLowerCase();
            
            // Add SameSite if missing
            if (!lowerCookie.contains("samesite")) {
                if (cookieHeader.contains("JSESSIONID")) {
                    cookieHeader += "; SameSite=Strict";
                } else {
                    cookieHeader += "; SameSite=Lax";
                }
            }
            
            // Add HttpOnly to JSESSIONID if missing
            if (cookieHeader.contains("JSESSIONID") && !lowerCookie.contains("httponly")) {
                cookieHeader += "; HttpOnly";
            }
            
            // Add Secure for HTTPS
            if (request.isSecure() && !lowerCookie.contains("secure")) {
                cookieHeader += "; Secure";
            }
            
            return cookieHeader;
        }
        
        /**
         * Final processing to catch any cookies set by the container
         */
        public void processFinalCookies() {
            Collection<String> cookieHeaders = originalResponse.getHeaders("Set-Cookie");
            if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
                // Clear and re-add all cookies with enhancements
                originalResponse.setHeader("Set-Cookie", null);
                
                for (String cookieHeader : cookieHeaders) {
                    String enhancedCookie = enhanceCookieHeader(cookieHeader);
                    originalResponse.addHeader("Set-Cookie", enhancedCookie);
                }
            }
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("Enhanced CookieSameSiteFilter initialized - bulletproof SameSite protection");
    }
    
    @Override
    public void destroy() {
        System.out.println("CookieSameSiteFilter destroyed");
    }
}
