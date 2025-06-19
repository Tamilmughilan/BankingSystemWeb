<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Banking System - Login</title>
<!-- Make sure this path is correct -->
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <div class="form-container">
            <h2>Banking System Login</h2>
            
           
            <% if (request.getAttribute("loginFailed") != null) { %>
                <p class="error">Login failed. Please try again.</p>
            <% } %>
            
            <% if (request.getAttribute("errorMessage") != null) { %>
                <p class="error-message"><%= request.getAttribute("errorMessage") %></p>
            <% } %>
            
            <!-- Login Form -->
            <form action="login" method="post" class="login-form <%= request.getAttribute("showOTPForm") != null ? "disabled" : "" %>">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" 
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>" 
                           required <%= request.getAttribute("showOTPForm") != null ? "readonly" : "" %>>
                </div>
                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" 
                           required <%= request.getAttribute("showOTPForm") != null ? "readonly" : "" %>>
                </div>
                <div class="form-group">
                    <button type="submit" <%= request.getAttribute("showOTPForm") != null ? "disabled" : "" %>>
                        Login
                    </button>
                </div>
            </form>
            
            <% if (request.getAttribute("showOTPForm") != null) { %>
                <div class="otp-section">
                    <% if (request.getAttribute("message") != null) { %>
                        <p class="success-message"><%= request.getAttribute("message") %></p>
                    <% } %>
                    
                    <% if (request.getAttribute("otpFailed") != null) { %>
                        <p class="error-message">Invalid OTP. Please try again.</p>
                    <% } %>
                    
                    <form action="login" method="post">
                        <input type="hidden" name="action" value="verifyOTP">
                        <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                        
                        <div class="form-group">
                            <label for="otp">Enter 6-digit OTP:</label>
                            <input type="text" id="otp" name="otp" maxlength="6" pattern="[0-9]{6}" 
                                   placeholder="000000" class="otp-input" required autocomplete="off">
                        </div>
                        
                        <div class="form-group">
                            <button type="submit">Verify OTP</button>
                        </div>
                    </form>
                </div>
            <% } %>
            
            <% if (request.getAttribute("showOTPForm") == null) { %>
                <div class="links">
                    <p>Don't have an account? <a href="signup.jsp">Sign up here</a></p>
                </div>
            <% } %>
        </div>
    </div>

   
</body>
</html>
