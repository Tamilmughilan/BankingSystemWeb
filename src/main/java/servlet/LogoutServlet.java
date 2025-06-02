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
            session.invalidate();

            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Logout Successful</title></head><body>" +
                "<h2>Logout Successful</h2>" +
                (userName != null ? "<p>Goodbye " + userName + "!</p>" : "") +
                "<p>You have been logged out successfully.</p>" +
                "<p><a href='" + contextPath + "/login.jsp'>Return to login page</a></p>" +
                "</body></html>"
            );
        } else {
            response.sendRedirect(contextPath + "/login.jsp");
        }
    }

}
