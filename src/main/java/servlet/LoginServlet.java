package servlet;

import javax.servlet.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

import entity.AuthenticationResult;
import service.AuthenticationService;
import storage.DataStorage;
import storage.DatabaseStorage;
import util.OTPUtil;

/**
 * Servlet that handles user authentication and login process.
 * Implements two factor authentication with password verification and OTP.
 * Redirects users to appropriate pages based on their roles.
 *
 * @author TAMIL MUGHILAN
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private DataStorage dataStorage;
    private AuthenticationService authService;
    
    /**
     * Initializes the servlet with database storage and authentication service.
     *
     * @throws ServletException if initialization fails
     */
    @Override
    public void init() throws ServletException {
        try {
			this.dataStorage = new DatabaseStorage();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.authService = new AuthenticationService(dataStorage);
    }
      
    /**
     * Handles GET requests by redirecting to login page.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
    
    /**
     * Handles POST requests for login and OTP verification.
     * Routes to appropriate handler based on action parameter.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("verifyOTP".equals(action)) {
            handleOTPVerification(request, response);
        } else {
            handleLogin(request, response);
        }
    }
    
    /**
     * Handles the initial login attempt with email and password.
     * Generates OTP if credentials are valid.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            AuthenticationResult result = authService.authenticate(email, password);
            
            if (result.isSuccess()) {
                //Storing user info in sesion
                HttpSession session = request.getSession();
                session.setAttribute("pendingAuth", result);
                session.setAttribute("pendingEmail", email);
                
                // Generate and display OTP 
                String otp = OTPUtil.generateOTP();
                System.out.println(" OTP for " + email + ": " + otp + " ");
                
                // Set attributes to show OTP form on same page
                request.setAttribute("showOTPForm", true);
                request.setAttribute("email", email);
                request.setAttribute("message", "Password verified! Please enter the OTP sent to your registered contact.");
                
                // Forward back to login page with OTP form visible
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                
            } else {
                request.setAttribute("loginFailed", true);
                request.setAttribute("email", email); // Preserve email
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("loginFailed", true);
            request.setAttribute("email", email); // Preserve email
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    /**
     * Handles OTP verification and completes the login process.
     * Creates user session and redirects based on role.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    private void handleOTPVerification(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String enteredOTP = request.getParameter("otp");
        HttpSession session = request.getSession();
        
        AuthenticationResult pendingAuth = (AuthenticationResult) session.getAttribute("pendingAuth");
        String email = (String) session.getAttribute("pendingEmail");
        
        if (pendingAuth == null) {
            request.setAttribute("loginFailed", true);
            request.setAttribute("errorMessage", "Session expired. Please login again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        if (OTPUtil.verifyOTP(enteredOTP)) {
            // OTP is valid, complete the login
            session.setAttribute("role", pendingAuth.getRole());
            session.setAttribute("userId", pendingAuth.getUserId());
            session.setAttribute("userName", pendingAuth.getUserName());
            
            // Clean up pending auth data
            session.removeAttribute("pendingAuth");
            session.removeAttribute("pendingEmail");
            
            // Redirect based on role - Updated to redirect customers to index.jsp
            String contextPath = request.getContextPath();
            switch (pendingAuth.getRole()) {
                case "CUSTOMER":
                    response.sendRedirect(contextPath + "/index.jsp");
                    break;
                case "EMPLOYEE":
                case "MANAGER":
                    response.sendRedirect(contextPath + "/index.jsp");
                    break;
                default:
                    response.sendRedirect(contextPath + "/login.jsp");
            }
            
        } else {
            // OTP is invalid show OTP form again with error
            request.setAttribute("showOTPForm", true);
            request.setAttribute("email", email);
            request.setAttribute("otpFailed", true);
            request.setAttribute("errorMessage", "Invalid OTP. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}