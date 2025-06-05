<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Banking System - Login</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <div class="form-container">
            <h2>Banking System Login</h2>
            <% if (request.getAttribute("loginFailed") != null) { %>
                <p class="error">Login failed. Please try again.</p>
            <% } %>
            <form action="login" method="post">
            <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" required>
                </div>
                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <div class="form-group">
                    <button type="submit">Login</button>
                </div>
            </form>
            <div class="links">
                <p>Don't have an account? <a href="signup.jsp">Sign up here</a></p>
            </div>
        </div>
    </div>
</body>
</html>
