package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;

public class ActivityLoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        
        // Skip logging for open paths
        if (path.equals("/login") || path.equals("/signup") || path.equals("/login.jsp") || path.equals("/signup.jsp")) {
            chain.doFilter(req, res);
            return;
        }
       
        chain.doFilter(req, res);

        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            String role = (String) session.getAttribute("role");
            
            if (userId != null) { // Only log if user is authenticated
                System.out.println("\nUser Activity Log:");
                System.out.println("Time: " + LocalDateTime.now());
                System.out.println("User ID: " + userId);
                System.out.println("Name: " + userName);
                System.out.println("Role: " + role);
                System.out.println("Activity: " + path);
                System.out.println("----------------------------------------");
            }
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) {}
    
    @Override
    public void destroy() {}
}
