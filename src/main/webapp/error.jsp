<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page isErrorPage="true"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Error - Banking System</title>
<link rel="stylesheet" href="style.css">
</head>
<body>
	<div class="error-container">
		<div class="error-icon">⚠️</div>

		<% 
            // Get error details safely
            Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
            
            // Default values if attributes are null
            if (statusCode == null) {
                statusCode = 500; // Default error code
            }
            
            String title = "Access Error";
            String message = "The requested page could not be accessed.";
            
            // Customize message based on error code
            switch (statusCode) {
                case 404:
                    title = "Page Not Found";
                    message = "The page you are looking for does not exist or has been moved.";
                    break;
                case 403:
                    title = "Access Forbidden";
                    message = "You do not have permission to access this resource.";
                    break;
                case 500:
                    title = "Server Error";
                    message = "An internal server error occurred. Please try again later.";
                    break;
                case 400:
                    title = "Bad Request";
                    message = "The request could not be processed due to invalid parameters.";
                    break;
                default:
                    title = "Access Error";
                    message = "An unexpected error occurred while processing your request.";
            }
        %>

		<div class="error-code">
			Error
			<%= statusCode %></div>
		<h1><%= title %></h1>
		<div class="error-message">
			<%= message %>
		</div>

		<div>
			<a href="${pageContext.request.contextPath}/" class="btn"> 🏠
				Return Home </a> <a href="javascript:history.back()"
				class="btn btn-secondary"> ← Go Back </a>
		</div>

		<div class="help-text">If you continue to experience issues,
			please contact support.</div>
	</div>

	<script nonce="<%= request.getAttribute("cspNonce") %>">
        // Optional: Auto-redirect after a delay for certain errors
        <% if (statusCode == 404) { %>
            setTimeout(function() {
                if (confirm('Would you like to be redirected to the home page?')) {
                    window.location.href = '${pageContext.request.contextPath}/';
                }
            }, 5000);
        <% } %>
    </script>
</body>
</html>