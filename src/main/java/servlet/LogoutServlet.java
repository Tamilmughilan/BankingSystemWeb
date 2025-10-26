package servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Enhanced Servlet that handles user logout and session invalidation.
 * Properly handles cookie security during logout process.
 *
 * @author TAMIL MUGHILAN
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    /**
     * Handles GET requests for logout.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    /**
     * Handles POST requests for logout.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    /**
     * Enhanced logout process with proper cookie handling.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        
        System.out.println("🔐 LOGOUT: Starting logout process");
        
        if (session != null) {
            String userName = (String) session.getAttribute("userName");
            String oldSessionId = session.getId();
            
            System.out.println("🔐 LOGOUT: User " + userName + " logging out, invalidating session: " + oldSessionId);
            
            // Invalidate the current session
            session.invalidate();
            
            // Clear the old JSESSIONID cookie explicitly
            Cookie jsessionCookie = new Cookie("JSESSIONID", "");
            jsessionCookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
            jsessionCookie.setMaxAge(0); // Delete the cookie
            jsessionCookie.setHttpOnly(true);
            response.addCookie(jsessionCookie);
            
            // Set cache control headers to prevent back button issues
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            System.out.println("🔐 LOGOUT: Redirecting to login page with logout=success");
            
            // Redirect to login page with logout success parameter
            // The login.jsp will handle creating a new secure session
            response.sendRedirect(contextPath + "/login.jsp?logout=success");
            
        } else {
            System.out.println("🔐 LOGOUT: No active session found, redirecting to login");
            response.sendRedirect(contextPath + "/login.jsp");
        }
    }
}