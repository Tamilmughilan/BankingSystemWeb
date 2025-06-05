package filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.http.*;

public class CSRFProtectionFilter implements Filter {
    
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "csrfToken";
    private SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String method = req.getMethod();
        String path = req.getServletPath();
        
        // Skip CSRF check for login, signup, and GET requests
        if (path.equals("/login") || path.equals("/signup") || 
            path.endsWith(".css") || "GET".equals(method)) {
            
            // Generate CSRF token for the session if it doesn't exist
            if (session != null && session.getAttribute(CSRF_TOKEN_ATTR) == null) {
                String token = generateCSRFToken();
                session.setAttribute(CSRF_TOKEN_ATTR, token);
                System.out.println("Generated new CSRF token for session");
            }
            
            chain.doFilter(request, response);
            return;
        }
        
        // validating CSRF token for post requests
        if ("POST".equals(method) && session != null) {
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
    
    private String generateCSRFToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
    
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("CSRF Protection Filter Initialized.");
    }
    
    @Override
    public void destroy() {}
}