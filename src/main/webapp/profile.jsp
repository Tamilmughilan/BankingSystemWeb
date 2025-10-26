<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>User Profile - Banking System</title>
<link rel="stylesheet" type="text/css" href="style.css">
<style>
</style>
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
				<a href="account.jsp" class="nav-button">Account Management</a> <a
					href="profile.jsp" class="nav-button active user-profile"> <%= session.getAttribute("userName") %>
					(<%= session.getAttribute("role") %>)
				</a> <a href="logout" class="nav-button logout">Logout</a>
			</div>
		</nav>

		<div class="profile-container">
			<!-- Profile Header -->
			<div class="profile-header">
				<div class="profile-avatar">
					<% 
                    String role = (String) session.getAttribute("role");
                    if ("CUSTOMER".equals(role)) { %>
					👤
					<% } else if ("MANAGER".equals(role)) { %>
					👨‍💼
					<% } else { %>
					👨‍💻
					<% } %>
				</div>
				<h1 class="profile-name"><%= session.getAttribute("userName") %></h1>
				<div class="profile-role role-<%= role.toLowerCase() %>">
					<%= role %>
				</div>
				<p style="margin-top: 15px; font-size: 1.1em; opacity: 0.9;">
					<% if ("CUSTOMER".equals(role)) { %>
					Banking customer with account access
					<% } else if ("MANAGER".equals(role)) { %>
					Banking system manager with full administrative privileges
					<% } else { %>
					Banking system employee
					<% } %>
				</p>
			</div>

			<!-- Profile Information Grid -->
			<div class="profile-grid">
				<!-- Basic Information -->
				<div class="profile-card">
					<h3 class="card-title">📋 Basic Information</h3>
					<div class="card-content">
						<div class="info-row">
							<div class="info-label">Username:</div>
							<div class="info-value"><%= session.getAttribute("userName") %></div>
						</div>

						<div class="info-row">
							<div class="info-label">User ID:</div>
							<div class="info-value"><%= session.getAttribute("userId") != null ? session.getAttribute("userId") : "N/A" %></div>
						</div>

						<div class="info-row">
							<div class="info-label">Role:</div>
							<div class="info-value">
								<span class="profile-role role-<%= role.toLowerCase() %>"
									style="padding: 4px 12px; font-size: 0.9em;"> <%= role %>
								</span>
							</div>
						</div>

						<div class="info-row">
							<div class="info-label">Account Status:</div>
							<div class="info-value" style="color: #28a745;">✅ Active</div>
						</div>
					</div>
				</div>

				<!-- Session Information -->
				<div class="profile-card">
					<h3 class="card-title">🔐 Session Details</h3>
					<div class="card-content">
						<div class="info-row">
							<div class="info-label">Login Time:</div>
							<div class="info-value">
								<%= session.getAttribute("loginTime") != null ? session.getAttribute("loginTime") : "Current Session" %>
							</div>
						</div>

						<div class="info-row">
							<div class="info-label">Session ID:</div>
							<div class="info-value"
								style="font-family: monospace; font-size: 0.8em;">
								<%= session.getId().substring(0, 8) %>...
							</div>
						</div>

						<div class="info-row">
							<div class="info-label">Security Level:</div>
							<div class="info-value" style="color: #28a745;">🔒 High
								Security</div>
						</div>

						<div class="info-row">
							<div class="info-label">Last Activity:</div>
							<div class="info-value">Just Now</div>
						</div>
					</div>
				</div>

				<!-- Role Permissions -->
				<div class="profile-card">
					<h3 class="card-title">🛡️ Permissions & Access</h3>
					<div class="card-content">
						<ul class="permissions-list">
							<% if ("CUSTOMER".equals(role)) { %>
							<li>💳 View Account Details</li>
							<li>💰 Deposit Money</li>
							<li>💸 Withdraw Money</li>
							<li>📊 View Transaction History</li>
							<li>👤 Update Profile Information</li>
							<% } else if ("EMPLOYEE".equals(role)) { %>
							<li>👥 View Customer Information</li>
							<li>✏️ Update Customer Details</li>
							<li>💳 Create New Accounts</li>
							<li>🔗 Manage Joint Accounts</li>
							<li>📊 View Account Details</li>
							<% } else if ("MANAGER".equals(role)) { %>
							<li>👥 Full Customer Management</li>
							<li>❌ Delete Customer Records</li>
							<li>💳 Create All Account Types</li>
							<li>🔗 Full Joint Account Management</li>
							<li>📊 Access All Reports</li>
							<li>⚙️ Administrative Functions</li>
							<% } %>
						</ul>
					</div>
				</div>
			</div>

			<!-- Quick Actions -->
			<div
				style="text-align: center; margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 10px;">
				<h3 style="margin-bottom: 20px; color: #333;">Quick Actions</h3>
				<div
					style="display: flex; gap: 15px; justify-content: center; flex-wrap: wrap;">
					<a href="index.jsp" class="button">🏠 Go to Dashboard</a>
					<% if ("CUSTOMER".equals(role)) { %>
					<a href="account.jsp" class="button">💳 My Accounts</a>
					<% } else { %>
					<a href="customer.jsp" class="button">👥 Customer Management</a> <a
						href="account.jsp" class="button">💳 Account Management</a>
					<% } %>
					<a href="logout" class="button" style="background: #dc3545;">🚪
						Logout</a>
				</div>
			</div>
		</div>
	</div>
</body>
</html>