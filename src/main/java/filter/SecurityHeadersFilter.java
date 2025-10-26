package filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
/**
 * Filter that adds security headers to HTTP responses which is sent to the browser.
 * Implements Content Security Policy, XSS protection, and other security measures.
 *
 * @author TAMIL MUGHILAN
 */
public class SecurityHeadersFilter implements Filter {
	
	
	 
	/**
	 * Processes responses to add security headers.
     * Generates nonce for CSP and applies various security headers.
     *
     * @param request the servlet request
     * @param response the servlet response  
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
	 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        
        
        
        // Generate nonce for this request
        String nonce = generateNonce();
        req.setAttribute("cspNonce", nonce);

        applySecurityHeaders(req, res, nonce);
        
        SecurityResponseWrapper responseWrapper = new SecurityResponseWrapper(res);
        
        chain.doFilter(request, responseWrapper);
        
        // Process the response to remove sensitive information
        String content = responseWrapper.getResponseContent();
        if (content != null) {
            // Remove sensitive comments and information
            content = removeSensitiveInformation(content);
            res.getWriter().write(content);
        }
    }
    
    /**
     * Applies security headers to the HTTP response.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @param nonce the generated nonce for CSP
     */
    private void applySecurityHeaders(HttpServletRequest req, HttpServletResponse res, String nonce) {
        // Check if this is an AJAX request
        String ajaxHeader = req.getHeader("X-Requested-With");
        boolean isAjaxRequest = "XMLHttpRequest".equals(ajaxHeader);
        
        // Clickjacking protection 
        res.setHeader("X-Frame-Options", "DENY");
        
        // XSS Protection
        res.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Content type sniffing protection
        res.setHeader("X-Content-Type-Options", "nosniff");
        
        // Referrer policy
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy with nonce 
        String cspPolicy = buildCSPPolicy(nonce);
        res.setHeader("Content-Security-Policy", cspPolicy);
        
        // HSTS for HTTPS requests
        if (req.isSecure()) {
            res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        
        // Permissions Policy
        res.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=(), payment=(), usb=()");
    }
    
    /**
     * Removes sensitive information from HTML responses that could help attackers.
     */
    private String removeSensitiveInformation(String content) {
        if (content == null) return null;
        
        // Remove HTML comments that might contain sensitive info
        content = content.replaceAll("<!--[\\s\\S]*?-->", "");
        
        // Remove JavaScript comments that might reveal sensitive info
        content = content.replaceAll("//.*?(?=\\n|\\r|$)", "");
        content = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        
        // Remove common debug/development comments
        content = content.replaceAll("(?i)\\b(user|username|password|admin|debug|test|dev|development)\\s*:", "redacted:");
        
        // Remove potential email addresses from comments (except in form values)
        content = content.replaceAll("(?<!value=\")\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "[email-redacted]");
        
        return content;
    }
    
    /**
     * Checks if the current page contains sensitive information.
     */
    private boolean isSensitivePage(String path) {
        return path != null && (
            path.contains("account") || 
            path.contains("profile") || 
            path.contains("customer") ||
            path.contains("admin")
        );
    }
    
    
    /**
     * Generates random nonce - number used once
     * @return nonce
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        new SecureRandom().nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
    
    /**
     * Builds the Content Security Policy header value.
     *
     * @param nonce the nonce to include in script-src
     * @return the complete CSP policy string
     */
    private String buildCSPPolicy(String nonce) {
        return "default-src 'self'; " +
        	   "script-src 'self' 'nonce-" + nonce + "'; " + 
               "style-src 'self'; "+
               "connect-src 'self'; " +
               "img-src 'self' data: blob:; " +
               "font-src 'self'; " +
               "object-src 'none'; " +
               "base-uri 'self'; " +
               "form-action 'self'; " +
               "frame-ancestors 'none'; " +
               "media-src 'none'; " +
               "frame-src 'none'; " +
               "child-src 'none'; " + 
               "worker-src 'none'; " +
               "manifest-src 'self'; " +
               "block-all-mixed-content;";
    }
    /**
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override 
    public void init(FilterConfig filterConfig) {
        System.out.println("Security Headers Filter Initialized");
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {}
    
    /**
     * Response wrapper to capture response content for processing.
     */
    private static class SecurityResponseWrapper extends HttpServletResponseWrapper {
        private java.io.StringWriter stringWriter = new java.io.StringWriter();
        private java.io.PrintWriter printWriter = new java.io.PrintWriter(stringWriter);
        
        public SecurityResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        
        public java.io.PrintWriter getWriter() throws IOException {
            return printWriter;
        }
        
        public String getResponseContent() {
            return stringWriter.toString();
        }
    }
}