<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%

HttpSession userSession = request.getSession(true); // Force session creation
String contextPath = request.getContextPath();

// Set secure JSESSIONID cookie manually
String cookieHeader = "JSESSIONID=" + userSession.getId() + 
                     "; Path=" + (contextPath.isEmpty() ? "/" : contextPath) + 
                     "; HttpOnly; SameSite=Strict";
response.setHeader("Set-Cookie", cookieHeader);

System.out.println("🏠 INDEX.JSP: Set secure cookie for session: " + userSession.getId());
%>
<!DOCTYPE html>
<html>
<head>
<title>Banking System</title>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
	<div class="container">
		<h1>Welcome to the Banking System</h1>
		<% if (session.getAttribute("userName") != null) { %>
		<p>
			Hello!
			<%= session.getAttribute("userName") %>!
		</p>
		<div class="menu">
			<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
			<a href="customer.jsp" class="button">Customer Management</a> <a
				href="account.jsp" class="button">Account Management</a>
			<% } else if ("CUSTOMER".equals(session.getAttribute("role"))) { %>
			<a href="account.jsp" class="button">My Account</a>
			<% } %>
			<a href="logout" class="button">Logout</a>
		</div>
		<% } else { %>
		<div class="menu">
			<a href="login.jsp" class="button">Login</a> <a href="signup.jsp"
				class="button">Sign Up</a>
		</div>
		<% } %>
	</div>
</body>
</html>