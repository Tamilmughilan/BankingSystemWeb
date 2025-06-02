package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class AuthenticationFilter implements Filter {
    
    private static final Set<String> openPaths = Set.of(
        "/login", "/signup", "/logout", 
        "/login.html", "/signup.html", "/style.css"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String contextPath = req.getContextPath();
        HttpSession session = req.getSession(false);
        
        System.out.println("AuthFilter: Processing path: " + path);
        
        // redirect to login if not authenticated
        if (path.equals("/") || path.equals("") || path.equals("/index.html")) {
            if (session != null && session.getAttribute("role") != null) {
                String role = (String) session.getAttribute("role");
                redirectBasedOnRole(role, res, contextPath);
                return;
            } else {
                res.sendRedirect(contextPath + "/login.html");
                return;
            }
        }
        
        // open paths and static resources
        if (openPaths.contains(path) || path.endsWith(".css") || path.endsWith(".js") || 
            path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif") ||
            path.endsWith(".ico")) {
            chain.doFilter(req, res);
            return;
        }
        
        // Check if user is logged in for protected resources
        if (session == null || session.getAttribute("role") == null) {
            System.out.println("AuthFilter: No valid session, redirecting to login");
            res.sendRedirect(contextPath + "/login.html");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        String action = req.getParameter("action");
        
        System.out.println("AuthFilter: User role: " + role + ", Action: " + action + ", Path: " + path);
        
        // Role-based access control
        switch (role) {
            case "MANAGER":
                // Managers have access to all operations
                chain.doFilter(req, res);
                break;
                
            case "EMPLOYEE":
                // Employees can do everything except delete
                if ("delete".equals(action)) {
                    sendAccessDenied(res, "Only managers can delete records.");
                    return;
                }
                // Employees cannot access account.html directly
                if (path.equals("/account.html")) {
                    sendAccessDenied(res, "Access denied. Employees use customer management interface.");
                    return;
                }
                chain.doFilter(req, res);
                break;
                
            case "CUSTOMER":
                // Customers are restricted to account operations only
                if (path.equals("/customer") || path.equals("/customer.html")) {
                    sendAccessDenied(res, "Access denied. Customers cannot access customer management.");
                    return;
                }
                // Only allow account-related paths for customers
                if (path.equals("/account") || path.equals("/account.html")) {
                    chain.doFilter(req, res);
                } else {
                    // if they access any other path
                    res.sendRedirect(contextPath + "/account.html");
                    return;
                }
                break;
                
            default:
                sendAccessDenied(res, "Invalid role.");
                return;
        }
    }
    
    private void redirectBasedOnRole(String role, HttpServletResponse response, String contextPath) throws IOException {
        switch (role) {
            case "CUSTOMER":
                response.sendRedirect(contextPath + "/account.html");
                break;
            case "EMPLOYEE":
            case "MANAGER":
                response.sendRedirect(contextPath + "/customer.html");
                break;
            default:
                response.sendRedirect(contextPath + "/login.html");
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
    
    @Override
    public void init(FilterConfig fConfig) {
        System.out.println("Authentication filter initialized");
    }
    
    @Override
    public void destroy() {}
}
