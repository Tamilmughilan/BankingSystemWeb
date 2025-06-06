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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private DataStorage dataStorage;
    private AuthenticationService authService;
    
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
      
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
    
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
            
            // Redirect based on role
            String contextPath = request.getContextPath();
            switch (pendingAuth.getRole()) {
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
            // OTP is invalid show OTP form again with error
            request.setAttribute("showOTPForm", true);
            request.setAttribute("email", email);
            request.setAttribute("otpFailed", true);
            request.setAttribute("errorMessage", "Invalid OTP. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}