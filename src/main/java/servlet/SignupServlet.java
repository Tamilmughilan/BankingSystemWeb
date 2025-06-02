package servlet;

import service.CustomerService;
import storage.DataStorage;
import storage.DatabaseStorage;
import util.PasswordUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    private DataStorage dataStorage;
    private CustomerService customerService;

    @Override
    public void init() throws ServletException {
        try {
            this.dataStorage = new DatabaseStorage();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Failed to initialize database storage", e);
        }
        this.customerService = new CustomerService(dataStorage);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // If user is already logged in, redirect them
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("role") != null) {
            String role = (String) session.getAttribute("role");
            String contextPath = request.getContextPath();
            switch (role) {
                case "CUSTOMER":
                    response.sendRedirect(contextPath + "/account.html");
                    break;
                case "EMPLOYEE":
                case "MANAGER":
                    response.sendRedirect(contextPath + "/customer.html");
                    break;
                default:
                    response.sendRedirect(contextPath + "/signup.html");
            }
            return;
        }
        
        // Show signup page
        response.sendRedirect(request.getContextPath() + "/signup.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String branchIdStr = request.getParameter("branchId");
        String contextPath = request.getContextPath();
        
        // Validation
        if (name == null || phone == null || email == null || password == null || 
            confirmPassword == null || branchIdStr == null ||
            name.trim().isEmpty() || phone.trim().isEmpty() || email.trim().isEmpty() || 
            password.trim().isEmpty() || branchIdStr.trim().isEmpty()) {
            
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup error</h2>" +
                "<p>Some fields are missing.</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup Error</h2>" +
                "<p>Passwords do not match.</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
            return;
        }
        
        if (password.length() < 6) {
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup Error</h2>" +
                "<p>Password must be at least 6 characters long.</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
            return;
        }
        
        int branchId;
        try {
            branchId = Integer.parseInt(branchIdStr.trim());
            if (branchId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup Error</h2>" +
                "<p>Invalid branch ID.</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
            return;
        }
        
        try {
            // Check if email already exists
            if (dataStorage.getCustomerByEmail(email.trim()) != null || 
                dataStorage.getEmployeeByEmail(email.trim()) != null) {
                response.setContentType("text/html");
                response.getWriter().println(
                    "<html><head><title>Signup Error</title></head><body>" +
                    "<h2>Signup Error</h2>" +
                    "<p>Email already exists. Please use a different email.</p>" +
                    "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                    "</body></html>"
                );
                return;
            }
            
            // Generate salt and hash password
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(password.trim(), salt);
            
            // Create customer
            int customerId = customerService.createCustomerWithPassword(
                name.trim(), phone.trim(), email.trim(), branchId, hashedPassword, salt);
            
            if (customerId > 0) {
                // Auto login after successful signup
                HttpSession session = request.getSession(true);
                session.setAttribute("role", "CUSTOMER");
                session.setAttribute("userId", customerId);
                session.setAttribute("userName", name.trim());
                session.setAttribute("email", email.trim());
                
                response.setContentType("text/html");
                response.getWriter().println(
                	    "<html><head><title>Signup Successful</title></head><body>" +
                	    "<h2>Signup Successful!</h2>" +
                	    "<p>Welcome " + name.trim() + "! Your customer ID is: " + customerId + "</p>" +
                	    "<p><a href='" + contextPath + "/account.html'>Go to your account</a></p>" +
                	    "</body></html>"
                	);

            } else {
                response.setContentType("text/html");
                response.getWriter().println(
                    "<html><head><title>Signup Failed</title></head><body>" +
                    "<h2>Signup Failed</h2>" +
                    "<p>Signup failed. Please try again.</p>" +
                    "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                    "</body></html>"
                );
            }
        } catch (IllegalArgumentException e) {
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup Error</h2>" +
                "<p>Signup failed: " + e.getMessage() + "</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println(
                "<html><head><title>Signup Error</title></head><body>" +
                "<h2>Signup Error</h2>" +
                "<p>Signup failed due to server error. Please try again.</p>" +
                "<p><a href='" + contextPath + "/signup.html'>Try Again</a></p>" +
                "</body></html>"
            );
        }
    }
}
