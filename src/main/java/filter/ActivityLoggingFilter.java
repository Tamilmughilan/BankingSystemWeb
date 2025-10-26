package filter;

import javax.servlet.*;


import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filter that logs user activities in the banking system
 * Records who did what
 * 
 * @author TAMIL MUGHILAN
 */

public class ActivityLoggingFilter implements Filter {
	
	
	/**
     * Filters requests and logs user activities after processing.
     * Skips logging for static files and open pages.
     *
     * @param request the servlet request
     * @param response the servlet response  
     * @param chain the filter chain to continue processing
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String uri = req.getRequestURI();
        
        // Skip logging for static resources
        if (isStaticResource(path) || isStaticResource(uri)) {
            chain.doFilter(req, res);
            return;
        }
        
        // Skip logging for open paths
        if (path.equals("/login") || path.equals("/signup") || 
            path.equals("/login.jsp") || path.equals("/signup.jsp") || 
            path.equals("/index.jsp")) {
            chain.doFilter(req, res);
            return;
        }
        
        chain.doFilter(req, res);
        
        HttpSession session = null;
        try {
            session = req.getSession(false);
            if (session != null) {
                Integer userId = (Integer) session.getAttribute("userId");
                String userName = (String) session.getAttribute("userName");
                String role = (String) session.getAttribute("role");
                
                if (userId != null) {
                    System.out.println("\nUser Activity Log:");
                    System.out.println("Time: " + LocalDateTime.now());
                    System.out.println("User ID: " + userId);
                    System.out.println("Name: " + userName);
                    System.out.println("Role: " + role);
                    System.out.println("Activity: " + path);
                    System.out.println("----------------------------------------");
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("ActivityLoggingFilter: Session was already invalidated, skipping activity log.");
        }
    }
    
    /**
     * Checks if the requested path is a static resource like CSS, JS, or images.
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
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Activity Logging Filter Initialized");
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    
    @Override
    public void destroy() {}
}