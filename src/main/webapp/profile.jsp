<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Profile - Banking System</title>
<!-- Make sure this path is correct -->
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <!-- Navigation Bar -->
        <nav class="top-navbar">
            <div class="nav-brand">
                <h2>Banking System</h2>
            </div>
            <div class="nav-links">
                <a href="index.jsp" class="nav-button">Home</a>
                <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
                    <a href="customer.jsp" class="nav-button">Customer Management</a>
                <% } %>
                <a href="account.jsp" class="nav-button">Account Management</a>
                <a href="profile.jsp" class="nav-button active user-profile">
                    <%= session.getAttribute("userName") %> (<%= session.getAttribute("role") %>)
                </a>
                <a href="logout" class="nav-button logout">Logout</a>
            </div>
        </nav>

        <!-- Page Header -->
        <div class="page-header">
            <h1>User Profile</h1>
        </div>

        <!-- Profile Information -->
        <div class="content-section">
            <h3>Profile Information</h3>
            
            <div class="profile-info">
                <div class="info-card">
                    <div class="info-row">
                        <div class="info-label">Username:</div>
                        <div class="info-value"><%= session.getAttribute("userName") %></div>
                    </div>
                    
                    <div class="info-row">
                        <div class="info-label">Role:</div>
                        <div class="info-value role-badge role-<%= session.getAttribute("role").toString().toLowerCase() %>">
                            <%= session.getAttribute("role") %>
                        </div>
                    </div>
                    
                    <div class="info-row">
                        <div class="info-label">User ID:</div>
                        <div class="info-value"><%= session.getAttribute("userId") != null ? session.getAttribute("userId") : "N/A" %></div>
                    </div>
                    
                    <div class="info-row">
                        <div class="info-label">Session Started:</div>
                        <div class="info-value">
                            <%= session.getAttribute("loginTime") != null ? session.getAttribute("loginTime") : "Current Session" %>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    
    </div>
</body>
</html>