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

public class SecurityHeadersFilter implements Filter {
	
	

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        
        
        
        // Generate nonce for this request
        String nonce = generateNonce();
        req.setAttribute("cspNonce", nonce);

        applySecurityHeaders(req, res, nonce);
        
        chain.doFilter(request, response);
    }
    
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
    
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        new SecureRandom().nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
    
    private String buildCSPPolicy(String nonce) {
        return "default-src 'self'; " +
               "script-src 'self' 'nonce-" + nonce + "' https://code.jquery.com https://cdnjs.cloudflare.com; " +
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
    
    @Override 
    public void init(FilterConfig filterConfig) {
        System.out.println("Security Headers Filter Initialized");
    }
    
    @Override
    public void destroy() {}
}