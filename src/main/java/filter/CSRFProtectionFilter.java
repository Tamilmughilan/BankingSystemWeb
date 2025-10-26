package filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Filter that protects against Cross-Site Request Forgery (CSRF) attacks.
 * Generates and validates CSRF tokens for POST requests.
 *
 * @author TAMIL MUGHILAN
 */
public class CSRFProtectionFilter implements Filter {
    
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "csrfToken";
    private SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Processes requests to validate CSRF tokens and generate new ones.
     * Blocks requests with invalid or missing CSRF tokens.
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
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String method = req.getMethod();
        String path = req.getServletPath();
        String uri = req.getRequestURI();
        
   
        if (isStaticResource(path) || isStaticResource(uri)) {
            System.out.println("CSRF Filter: Allowing static resource: " + uri);
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = req.getSession(true); // Create session if it doesn't exist
        
        //
        if (session.getAttribute(CSRF_TOKEN_ATTR) == null) {
            String token = generateCSRFToken();
            session.setAttribute(CSRF_TOKEN_ATTR, token);
            System.out.println("Generated new CSRF token for session");
        }
        
        // Skip CSRF check for login, signup, and GET requests
        if (path.equals("/login") || path.equals("/signup") || "GET".equals(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Validate CSRF token for POST requests
        if ("POST".equals(method)) {
            String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
            String requestToken = req.getParameter(CSRF_TOKEN_PARAM);
            
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                System.out.println("CSRF token validation failed. Session: " + sessionToken + ", Request: " + requestToken);
                res.setStatus(403);
                res.setContentType("text/html");
                res.getWriter().println(
                    "<html><body>" +
                    "<h2>Security Error</h2>" +
                    "<p>Invalid request. Please refresh the page and try again.</p>" +
                    "<a href='javascript:history.back()'>Go Back</a>" +
                    "</body></html>"
                );
                return;
            }
        }
        
        System.out.println("CSRF Protection passed, moving to next filter");
        chain.doFilter(request, response);
    }
    
    /**
     * Checks if the requested path is a static resource.
     *
     * @param path the request path to check
     * @return true if it's a static resource, false otherwise
     */
    private boolean isStaticResource(String path) {
        if (path == null) return false;
        String lowercasePath = path.toLowerCase();
        return lowercasePath.endsWith(".css") || 
               lowercasePath.endsWith(".js") || 
               lowercasePath.endsWith(".png") || 
               lowercasePath.endsWith(".jpg") || 
               lowercasePath.endsWith(".jpeg") || 
               lowercasePath.endsWith(".gif") ||
               lowercasePath.endsWith(".ico") ||
               lowercasePath.endsWith(".svg") ||
               lowercasePath.endsWith(".woff") ||
               lowercasePath.endsWith(".woff2") ||
               lowercasePath.endsWith(".ttf") ||
               lowercasePath.endsWith(".eot");
    }
    
    /**
     * Generates a secure random CSRF token.
     *
     * @return a Base64 encoded CSRF token
     */
    private String generateCSRFToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
    /**
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("CSRF Protection Filter Initialized.");
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {}
}