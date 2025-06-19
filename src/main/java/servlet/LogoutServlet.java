package servlet;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();
        
        if (session != null) {
            String userName = (String) session.getAttribute("userName");
            // Invalidate the current session
            session.invalidate();
            
            // Set cache control headers to prevent back button issues
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            // Redirect to login page instead of showing logout message
            // This ensures a fresh session and CSRF token are created
            response.sendRedirect(contextPath + "/login.jsp?logout=success");
        } else {
            response.sendRedirect(contextPath + "/login.jsp");
        }
    }
}