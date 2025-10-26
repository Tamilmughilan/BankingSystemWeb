package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * Filter that checks Authentication and Authorization
 * Redirects unauthenticated users to login page
 * Enforces role based access control
 * 
 * @author TAMIL MUGHILAN
 */
public class AuthenticationFilter implements Filter {
    
    private static final Set<String> openPaths = Set.of(
        "/login", "/signup", "/logout", 
        "/login.jsp", "/signup.jsp", "/index.jsp",
        "/style.css"
    );
    
    /**
     * Processes requests to check Authentication and Authorization.
     * Allows access based on user role and user requests.
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

        String path = req.getServletPath();
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
       
        // Allow API endpoints - check both path and URI
        if (path.startsWith("/api/") || uri.contains("/api/")) {
            System.out.println("AuthFilter: Allowing API endpoint: " + path + " (URI: " + uri + ")");
            chain.doFilter(req, res);
            return;
        }
  
        if (isStaticResource(uri) || isStaticResource(path)) {
            System.out.println("AuthFilter: Allowing static resource: " + uri);
            chain.doFilter(req, res);
            return;
        }

        if (openPaths.contains(path)) {
            System.out.println("AuthFilter: Allowing open path: " + path);
            chain.doFilter(req, res);
            return;
        }

        if (path.equals("/") || path.equals("")) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("role") != null) {
                String role = (String) session.getAttribute("role");
                redirectBasedOnRole(role, res, contextPath);
                return;
            } else {
                res.sendRedirect(contextPath + "/index.jsp");
                return;
            }
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            System.out.println("AuthFilter: No valid session for path: " + path + ", redirecting to login");
            res.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        String action = req.getParameter("action");

        switch (role) {
            case "MANAGER":
                chain.doFilter(req, res);
                break;
            case "EMPLOYEE":
                if ("delete".equals(action)) {
                    sendAccessDenied(res, "Only managers can delete records.");
                    return;
                }
                chain.doFilter(req, res);
                break;
            case "CUSTOMER":
                if (path.equals("/customer") || path.equals("/customer.jsp")) {
                    sendAccessDenied(res, "Access denied. Customers cannot access customer management.");
                    return;
                }
                if (path.equals("/account") || path.equals("/account.jsp")) {
                    chain.doFilter(req, res);
                } else {
                    res.sendRedirect(contextPath + "/account.jsp");
                }
                break;
            default:
                sendAccessDenied(res, "Invalid role.");
        }
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
               lowercasePath.endsWith(".eot") ||
               lowercasePath.endsWith(".html"); // Add .html for dashboard
    }
    
    /**
     * Redirects user to appropriate page based on their role.
     *
     * @param role the user's role
     * @param response the HTTP response
     * @param contextPath the application context path
     * @throws IOException if redirection fails
     */
    private void redirectBasedOnRole(String role, HttpServletResponse response, String contextPath) throws IOException {
        switch (role) {
            case "CUSTOMER":
                response.sendRedirect(contextPath + "/account.jsp");
                break;
            case "EMPLOYEE":
            case "MANAGER":
                response.sendRedirect(contextPath + "/index.jsp");
                break;
            default:
                response.sendRedirect(contextPath + "/login.jsp");
        }
    }
    
    private void sendAccessDenied(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println(
            "<html><head><title>Access Denied</title></head><body>" +
            "<h2>Access Denied</h2>" +
            "<p>" + message + "</p>" +
            "<p><a href='javascript:history.back()'>Go Back</a> | <a href='logout'>Logout</a></p>" +
            "</body></html>"
        );
    }
    /**
     * Initializes the filter when application starts.
     *
     * @param fConfig the filter configuration
     */
    @Override
    public void init(FilterConfig fConfig) {
        System.out.println("Authentication filter initialized");
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {}
}