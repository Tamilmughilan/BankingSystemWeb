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
        
        HttpSession session = req.getSession(false);
        String path = req.getServletPath();
        
        if (session != null) {
            String role = (String) session.getAttribute("role");
            int userId = (int) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            
            String time = LocalDateTime.now().toString();
            
            System.out.println("User Activity Log:");
            System.out.println("Time: " + time);
            System.out.println("User ID: " + userId);
            System.out.println("Name: " + userName);
            System.out.println("Role: " + role);
            System.out.println("Activity: " + path);
            System.out.println("----------------------------------------");
        }
        
        System.out.println("\nActivity logging filter moving to authentication");
        chain.doFilter(request, response);
    }
    
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("\nActivity logging Filter Initialized second.");
    }
    
    @Override
    public void destroy() {}
}