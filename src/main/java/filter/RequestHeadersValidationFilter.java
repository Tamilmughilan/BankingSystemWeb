package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enhanced filter that validates HTTP request headers and prevents sensitive information 
 * exposure in URLs (addresses "Information Disclosure - Sensitive Information in URL").
 * 
 * @author TAMIL MUGHILAN
 */
public class RequestHeadersValidationFilter implements Filter {
    
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081",
        "http://localhost:8081",
        "http://banking.local:8080" 
    );
    
    // Patterns for sensitive information that shouldn't be in URLs
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{10,}\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-?\\d{2}-?\\d{4}\\b");
    
    private static final String AJAX_HEADER = "X-Requested-With";
    private static final String AJAX_HEADER_VALUE = "XMLHttpRequest";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String uri = req.getRequestURI(); 
        String method = req.getMethod();
        String queryString = req.getQueryString();
        
        // Allow static files first 
        if (isStaticResource(uri)) {
            System.out.println("RequestHeadersValidation: Allowing static resource: " + uri);
            chain.doFilter(request, response);
            return;
        }
        
        // Check for sensitive information in URL parameters
        if (queryString != null && containsSensitiveInformation(queryString)) {
            System.out.println("Blocked request with sensitive information in URL: " + uri + "?" + queryString);
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Invalid request parameters\"}");
            return;
        }
        
        // No validation needed for open paths
        if (isOpenPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Validation for POST requests
        if ("POST".equals(method)) {
            if (!validateOrigin(req, res)) {
                return; // Block the request
            }
        }
        
        // Validation for AJAX requests
        if (isAjaxRequest(req)) {
            if (!validateAjaxHeaders(req, res)) {
                return; // Block
            }
        }
        
        // Validate content type for POST methods
        if ("POST".equals(method) && !validateContentType(req, res)) {
            return; // Block the request
        }
        
        System.out.println("Headers validation passed for: " + path);
        chain.doFilter(request, response);
    }
    
    /**
     * Checks if the URL contains sensitive information that shouldn't be exposed.
     */
    private boolean containsSensitiveInformation(String queryString) {
        if (queryString == null) return false;
        
        String decodedQuery = java.net.URLDecoder.decode(queryString, java.nio.charset.StandardCharsets.UTF_8);
        
        // Check for email addresses
        if (EMAIL_PATTERN.matcher(decodedQuery).find()) {
            return true;
        }
        
        // Check for phone numbers
        if (PHONE_PATTERN.matcher(decodedQuery).find()) {
            return true;
        }
        
        // Check for SSN patterns
        if (SSN_PATTERN.matcher(decodedQuery).find()) {
            return true;
        }
        
        // Check for common sensitive parameter names
        String lowerQuery = decodedQuery.toLowerCase();
        return lowerQuery.contains("password") || 
               lowerQuery.contains("ssn") || 
               lowerQuery.contains("creditcard") ||
               lowerQuery.contains("pin");
    }
    
    private boolean isOpenPath(String path) {
        return path.equals("/login") || path.equals("/signup") || 
               path.equals("/login.jsp") || path.equals("/signup.jsp") || 
               path.equals("/logout");
    }
    
    private boolean isStaticResource(String path) {
        if (path == null) return false;
        return path.endsWith(".css") || path.endsWith(".js") || 
               path.endsWith(".png") || path.endsWith(".jpeg") ||
               path.endsWith(".jpg") || path.endsWith(".gif") ||
               path.endsWith(".ico") || path.endsWith(".svg") ||
               path.endsWith(".woff") || path.endsWith(".woff2") ||
               path.endsWith(".ttf") || path.endsWith(".eot");
    }
    
    private boolean validateOrigin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String origin = req.getHeader("Origin");
        String referer = req.getHeader("Referer");
        
        if (origin != null) {
            if (!ALLOWED_ORIGINS.contains(origin)) {
                System.out.println("Invalid Origin header: " + origin);
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.getWriter().write("Origin not allowed");
                return false;
            }
        } else if (referer != null) {
            boolean validReferer = false;
            for (String allowedOrigin : ALLOWED_ORIGINS) {
                if (referer.startsWith(allowedOrigin)) {
                    validReferer = true;
                    break;
                }
            }
            if (!validReferer) {
                System.out.println("Invalid Referer header: " + referer);
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.getWriter().write("Referer not allowed");
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
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid AJAX request");
            return false;
        }
        
        return true;
    }
    
    private boolean validateContentType(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String contentType = req.getContentType();
        
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            if (lowerContentType.contains("application/x-www-form-urlencoded") ||
                lowerContentType.contains("multipart/form-data") ||
                lowerContentType.contains("application/json")) {
                return true;
            }
        }
        
        System.out.println("Invalid Content-Type: " + contentType);
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid content type");
        return false;
    }
    
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Enhanced Request Headers Validation Filter Initialized");
    }
    
    @Override
    public void destroy() {}
}
