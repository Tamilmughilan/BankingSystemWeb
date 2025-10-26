<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%
// ===============================================================================
// SECURE COOKIE FIX - Force secure JSESSIONID cookie for login page
// ===============================================================================
HttpSession userSession = request.getSession(true); // Force session creation
String contextPath = request.getContextPath();

// Set secure JSESSIONID cookie manually
String cookieHeader = "JSESSIONID=" + userSession.getId() + 
                     "; Path=" + (contextPath.isEmpty() ? "/" : contextPath) + 
                     "; HttpOnly; SameSite=Strict";
response.setHeader("Set-Cookie", cookieHeader);

// Check if this is a logout redirect
String logoutParam = request.getParameter("logout");
if ("success".equals(logoutParam)) {
    System.out.println("🔐 LOGIN.JSP: Logout redirect - Set secure cookie for new session: " + userSession.getId());
} else {
    System.out.println("🔐 LOGIN.JSP: Normal access - Set secure cookie for session: " + userSession.getId());
}
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Banking System - Login</title>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
	<div class="container">
		<div class="form-container">
			<h2>Banking System Login</h2>

			<!-- Show logout success message -->
			<% if ("success".equals(request.getParameter("logout")) && request.getParameter("logout") != null && request.getParameter("logout").length() <= 7) { %>
			<p class="success-message">You have been successfully logged out.</p>
			<% } %>

			<% if (request.getAttribute("loginFailed") != null) { %>
			<p class="error">Login failed. Please try again.</p>
			<% } %>

			<% if (request.getAttribute("errorMessage") != null) { %>
			<p class="error-message"><%= request.getAttribute("errorMessage") %></p>
			<% } %>

			<!-- Login Form -->
			<form action="login" method="post"
				class="login-form <%= request.getAttribute("showOTPForm") != null ? "disabled" : "" %>">
				<input type="hidden" name="csrfToken"
					value="<%= session.getAttribute("csrfToken") %>">
				<div class="form-group">
					<label for="email">Email:</label> <input type="email" id="email"
						name="email"
						value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
						required
						<%= request.getAttribute("showOTPForm") != null ? "readonly" : "" %>>
				</div>
				<div class="form-group">
					<label for="password">Password:</label> <input type="password"
						id="password" name="password" required
						<%= request.getAttribute("showOTPForm") != null ? "readonly" : "" %>>
				</div>
				<div class="form-group">
					<button type="submit"
						<%= request.getAttribute("showOTPForm") != null ? "disabled" : "" %>>
						Login</button>
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
					<input type="hidden" name="action" value="verifyOTP"> <input
						type="hidden" name="csrfToken"
						value="<%= session.getAttribute("csrfToken") %>">

					<div class="form-group">
						<label for="otp">Enter 6-digit OTP:</label> <input type="text"
							id="otp" name="otp" maxlength="6" pattern="[0-9]{6}"
							placeholder="000000" class="otp-input" required
							autocomplete="off">
					</div>

					<div class="form-group">
						<button type="submit">Verify OTP</button>
					</div>
				</form>
			</div>
			<% } %>

			<% if (request.getAttribute("showOTPForm") == null) { %>
			<div class="links">
				<p>
					Don't have an account? <a href="signup.jsp">Sign up here</a>
				</p>
			</div>
			<% } %>
		</div>
	</div>
</body>
</html>