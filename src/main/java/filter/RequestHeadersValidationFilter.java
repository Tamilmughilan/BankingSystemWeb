package filter;

import javax.servlet.*;


import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
public class RequestHeadersValidationFilter implements Filter {
    
    //origins allowed for now
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081",
        "http://localhost:8081",
        "http://banking.local:8080" 
    );
    
    //Required headers
    private static final String AJAX_HEADER = "X-Requested-With";
    private static final String AJAX_HEADER_VALUE = "XMLHttpRequest";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String uri = req.getRequestURI(); // Add this line
        String method = req.getMethod();
        
        // ✅ ALLOW STATIC FILES FIRST (This was missing!)
        if (uri.endsWith(".css") || uri.endsWith(".js") ||
            uri.endsWith(".png") || uri.endsWith(".jpg") ||
            uri.endsWith(".jpeg") || uri.endsWith(".gif") ||
            uri.endsWith(".ico")) {
            System.out.println("RequestHeadersValidation: Allowing static resource: " + uri);
            chain.doFilter(request, response);
            return;
        }
        
        //No validation needed for these paths
        if (isOpenPath(path) || isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        //Validation for post requests
        if ("POST".equals(method)) {
            if (!validateOrigin(req, res)) {
                return; //Block the request
            }
        }
        
        //Validation for ajax requests
        if (isAjaxRequest(req)) {
            if (!validateAjaxHeaders(req, res)) {
                return; //Block
            }
        }
        
        //Validate the content for post methods
        if ("POST".equals(method) && !validateContentType(req, res)) {
            return;//Block the req
        }
        
        System.out.println("Headers validation passed for: " + path);
        chain.doFilter(request, response);
    }
    
    private boolean isOpenPath(String path) {
        return path.equals("/login") || path.equals("/signup") || 
               path.equals("/login.jsp") || path.equals("/signup.jsp") || 
               path.equals("/logout");
    }
    
    private boolean isStaticResource(String path) {
        return path.endsWith(".css") || path.endsWith(".js") || 
               path.endsWith(".png") || path.endsWith(".jpeg");
    }
    
    private boolean validateOrigin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String origin = req.getHeader("Origin");
        String referer = req.getHeader("Referer");
        
        
        if (origin != null) {
            if (!ALLOWED_ORIGINS.contains(origin)) {
                System.out.println("Invalid Origin header: " + origin);
                
                return false;
            }
        }
        
        else if (referer != null) {
            boolean validReferer = false;
            for (String allowedOrigin : ALLOWED_ORIGINS) {
                if (referer.startsWith(allowedOrigin)) {
                    validReferer = true;
                    break;
                }
            }
            if (!validReferer) {
                System.out.println("Invalid Referer header: " + referer);
               
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isAjaxRequest(HttpServletRequest req) {
        String ajaxHeader = req.getHeader(AJAX_HEADER);
        return AJAX_HEADER_VALUE.equals(ajaxHeader);
    }
    
    private boolean validateAjaxHeaders(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String ajaxHeader = req.getHeader(AJAX_HEADER);
        
        if (!AJAX_HEADER_VALUE.equals(ajaxHeader)) {
            System.out.println("Invalid AJAX header: " + ajaxHeader);
            
            return false;
        }
        
        return true;
    }
    
    private boolean validateContentType(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String contentType = req.getContentType();
        
        // Allow form submissions, AJAX requests
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            if (lowerContentType.contains("application/x-www-form-urlencoded") ||
                lowerContentType.contains("multipart/form-data")) {
                return true;
            }
        }
        
        //block the request for others
        System.out.println("Invalid Content-Type: " + contentType);
        
        return false;
    }
    
   
    
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Request Headers Validation Filter Initialized");
    }
    
    @Override
    public void destroy() {}
}