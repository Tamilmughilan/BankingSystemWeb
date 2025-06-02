package servlet;
import service.AuthenticationService;

import storage.DataStorage;
import storage.DatabaseStorage;
import entity.AuthenticationResult;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private DataStorage dataStorage;
    //Uses the Authentication entity for role based features
    private AuthenticationService authService;

    @Override
    public void init() throws ServletException {
        try {
            this.dataStorage = new DatabaseStorage();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Failed to initialize database storage", e);
        }
        this.authService = new AuthenticationService(dataStorage);
    }
    
    //Takes to user to the dashboard respective to their role - SOLID principle
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already logged in, redirect to appropriate dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("role") != null) {
            String role = (String) session.getAttribute("role");
            String contextPath = request.getContextPath();
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
            return;
        }

        // Show login page
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath();

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Login Error</title></head><body>" +
                "<h2>Login Error</h2>" +
                "<p>Email and password are required.</p>" +
                "<p><a href='" + contextPath + "/login.jsp'>Try Again</a></p>" +
                "</body></html>"
            );
            return;
        }

        try {
            AuthenticationResult result = authService.authenticate(email.trim(), password.trim());

            if (result.isSuccess()) {
                HttpSession session = request.getSession(true);
                session.setAttribute("role", result.getRole());
                session.setAttribute("userId", result.getUserId());
                session.setAttribute("userName", result.getUserName());
                session.setAttribute("email", email.trim());

                // Redirect based on role
                switch (result.getRole()) {
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
            } else {
                response.setContentType("text/html");
                response.getWriter().println(
                    "<html><head><title>Login Failed</title></head><body>" +
                    "<h2>Login Failed</h2>" +
                    "<p>Invalid credentials. Try again</p>" +
                    "<p><a href='" + contextPath + "/login.jsp'>Try Again</a></p>" +
                    "</body></html>"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Login Error</title></head><body>" +
                "<h2>Login Error</h2>" +
                "<p>Login failed due to server error, Please try again.</p>" +
                "<p><a href='" + contextPath + "/login.jsp'>Try Again</a></p>" +
                "</body></html>"
            );
        }
    }
}
