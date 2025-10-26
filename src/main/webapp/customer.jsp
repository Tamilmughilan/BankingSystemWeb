<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html lang="en">
<head>
<script src="js/jquery-3.6.0.min.js"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Customer Management</title>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
</head>
<body>
	<div class="container">

		<nav class="top-navbar">
			<div class="nav-brand">
				<h2>Banking System</h2>
			</div>
			<div class="nav-links">
				<a href="index.jsp" class="nav-button">Home</a>
				<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
				<a href="customer.jsp" class="nav-button active">Customer
					Management</a>
				<% } %>
				<a href="account.jsp" class="nav-button">Account Management</a> <a
					href="profile.jsp" class="nav-button user-profile"> <%= session.getAttribute("userName") %>
					(<%= session.getAttribute("role") %>)
				</a> <a href="logout" class="nav-button logout">Logout</a>
			</div>
		</nav>


		<div class="page-header">
			<h1>Customer Management</h1>
		</div>


		<div class="operation-buttons">
			<button id="view-customers-btn" class="operation-btn">View
				Customers</button>
			<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
			<button id="update-customer-btn" class="operation-btn">Update
				Customer</button>
			<% } %>
			<% if ("MANAGER".equals(session.getAttribute("role"))) { %>
			<button id="delete-customer-btn" class="operation-btn">Delete
				Customer</button>
			<% } %>
		</div>


		<div id="result-message"></div>
		<div id="customer-details"></div>
		<div id="customer-list"></div>


		<% if (request.getAttribute("successMessage") != null) { %>
		<div class="success-message">
			<h3><%= request.getAttribute("successMessage") %></h3>
			<% if (request.getAttribute("showUpdateResult") != null || request.getAttribute("showDeleteResult") != null) { %>
			<p>
				<strong>Storage Type:</strong>
				<%= request.getAttribute("storageType") %></p>
			<% } %>
		</div>
		<% } %>

		<% if (request.getAttribute("errorMessage") != null) { %>
		<div class="error-message">
			<h3><%= request.getAttribute("errorMessage") %></h3>
		</div>
		<% } %>


		<div id="view-customers" class="content-section">
			<h3>View Customers</h3>

			<div class="view-tabs">
				<button id="single-tab-btn" class="tab-btn active">Single
					Customer</button>

				<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
				<button id="branch-tab-btn" class="tab-btn">By Branch</button>
				<% } %>
			</div>

			<div id="single" class="tab-content active">
				<h4>View Customer Details</h4>
				<form id="viewCustomerForm" action="customer" method="get"
					class="form-container">
					<input type="hidden" name="action" value="get"> <input
						type="hidden" name="storageType" value="database">

					<div class="form-group">
						<label for="customerId">Customer ID:</label> <input type="number"
							id="customerId" name="customerId" required
							placeholder="Enter customer ID">
					</div>

					<button type="submit" class="submit-btn">View Customer</button>
				</form>
			</div>




			<!-- Branch View -->
			<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
			<div id="branch" class="tab-content">
				<h4>View Customers by Branch</h4>
				<form id="viewCustomersByBranchForm" action="customer" method="get"
					class="form-container">
					<input type="hidden" name="action" value="getByBranch"> <input
						type="hidden" name="storageType" value="database">

					<div class="form-group">
						<label for="branchIdFilter">Branch:</label> <select
							id="branchIdFilter" name="branchId" required>
							<option value="">Select Branch</option>
							<option value="1">Main Branch</option>
							<option value="2">Anna Nagar Branch</option>
							<option value="3">Adyar Branch</option>
						</select>
					</div>

					<button type="submit" class="submit-btn">View Branch
						Customers</button>
				</form>
			</div>
			<% } %>
		</div>
		<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
		<div id="update-customer" class="content-section">
			<h3>Update Customer Information</h3>
			<form id="updateCustomerForm" action="customer" method="post"
				class="form-container">
				<input type="hidden" name="action" value="update"> <input
					type="hidden" name="csrfToken"
					value="<%= session.getAttribute("csrfToken") %>"> <input
					type="hidden" name="storageType" value="database">

				<div class="form-row">
					<div class="form-group">
						<label for="updateCustomerId">Customer ID:</label> <input
							type="number" id="updateCustomerId" name="customerId" required
							placeholder="Enter customer ID">
					</div>
					<div class="form-group">
						<label for="updateName">Full Name:</label> <input type="text"
							id="updateName" name="name" required
							placeholder="Enter full name">
					</div>
				</div>

				<div class="form-row">
					<div class="form-group">
						<label for="updatePhone">Phone Number:</label> <input type="tel"
							id="updatePhone" name="phone" required
							placeholder="Enter phone number">
					</div>
					<div class="form-group">
						<label for="updateEmail">Email Address:</label> <input
							type="email" id="updateEmail" name="email" required
							placeholder="Enter email address">
					</div>
				</div>

				<div class="form-group">
					<label for="updateBranchId">Branch:</label> <select
						id="updateBranchId" name="branchId" required>
						<option value="">Select Branch</option>
						<option value="1">Main Branch</option>
						<option value="2">Anna Nagar Branch</option>
						<option value="3">Adyar Branch</option>
					</select>
				</div>

				<button type="submit" class="submit-btn">Update Customer</button>
			</form>
		</div>
		<% } %>


		<% if ("MANAGER".equals(session.getAttribute("role"))) { %>
		<div id="delete-customer" class="content-section">
			<h3>Delete Customer</h3>
			<div class="warning-message">
				<p>⚠️ Warning: This action cannot be undone. Customer must not
					have any active accounts.</p>
			</div>
			<form id="deleteCustomerForm" action="customer" method="post"
				class="form-container">
				<input type="hidden" name="action" value="delete"> <input
					type="hidden" name="csrfToken"
					value="<%= session.getAttribute("csrfToken") %>"> <input
					type="hidden" name="storageType" value="database">

				<div class="form-group">
					<label for="deleteCustomerId">Customer ID:</label> <input
						type="number" id="deleteCustomerId" name="customerId" required
						placeholder="Enter customer ID to delete">
				</div>

				<button type="submit" class="submit-btn danger">Delete
					Customer</button>
			</form>
		</div>
		<% } %>


		<% if (request.getAttribute("showCustomerDetails") != null && request.getAttribute("customer") != null) { %>
		<div class="customer-details">
			<h3>Customer Details</h3>
			<p>
				<strong>Storage Type:</strong>
				<%= request.getAttribute("storageType") %></p>
			<div class="customer-info">
				<%= request.getAttribute("customer").toString().replace("\n", "<br>") %>
			</div>
		</div>
		<% } %>

		<% if (request.getAttribute("showCustomersList") != null && request.getAttribute("customers") != null) { %>
		<div class="customers-list">
			<h3>Customers List</h3>
			<p>
				<strong>Storage Type:</strong>
				<%= request.getAttribute("storageType") %></p>
			<div class="customers-info">
				<% 
                    java.util.List<?> customers = (java.util.List<?>) request.getAttribute("customers");
                    for (Object customer : customers) {
                    %>
				<div class="customer-item">
					<%= customer.toString().replace("\n", "<br>") %>
				</div>
				<hr>
				<% } %>
			</div>
		</div>
		<% } %>
	</div>

	<script nonce="<%= request.getAttribute("cspNonce") %>">
        // Section navigation functions
        function showSection(sectionId) {
            // Hide all sections
            const sections = document.querySelectorAll('.content-section');
            sections.forEach(section => section.style.display = 'none');
            
            // Show selected section
            document.getElementById(sectionId).style.display = 'block';
            
            // Update button states
            const buttons = document.querySelectorAll('.operation-btn');
            buttons.forEach(btn => btn.classList.remove('active'));
            
            // Find and activate the corresponding button
            const buttonId = sectionId + '-btn';
            const activeButton = document.getElementById(buttonId);
            if (activeButton) {
                activeButton.classList.add('active');
            }
        }

        // Tab navigation function
        function showViewTab(tabId) {
            const tabs = document.querySelectorAll('#view-customers .tab-content');
            tabs.forEach(tab => tab.classList.remove('active'));
            
            const buttons = document.querySelectorAll('#view-customers .tab-btn');
            buttons.forEach(btn => btn.classList.remove('active'));
            
            document.getElementById(tabId).classList.add('active');
            
            // Find and activate the corresponding tab button
            const buttonId = tabId + '-tab-btn';
            const activeButton = document.getElementById(buttonId);
            if (activeButton) {
                activeButton.classList.add('active');
            }
        }

        // Event listeners for operation buttons
        document.addEventListener('DOMContentLoaded', function() {
            // Main operation buttons
            const viewCustomersBtn = document.getElementById('view-customers-btn');
            if (viewCustomersBtn) {
                viewCustomersBtn.addEventListener('click', function() {
                    showSection('view-customers');
                });
            }

            const updateCustomerBtn = document.getElementById('update-customer-btn');
            if (updateCustomerBtn) {
                updateCustomerBtn.addEventListener('click', function() {
                    showSection('update-customer');
                });
            }

            const deleteCustomerBtn = document.getElementById('delete-customer-btn');
            if (deleteCustomerBtn) {
                deleteCustomerBtn.addEventListener('click', function() {
                    showSection('delete-customer');
                });
            }

            // Tab buttons
            const singleTabBtn = document.getElementById('single-tab-btn');
            if (singleTabBtn) {
                singleTabBtn.addEventListener('click', function() {
                    showViewTab('single');
                });
            }

         

            const branchTabBtn = document.getElementById('branch-tab-btn');
            if (branchTabBtn) {
                branchTabBtn.addEventListener('click', function() {
                    showViewTab('branch');
                });
            }
        });

    $(document).ready(function() {
        var csrfToken = '<%= session.getAttribute("csrfToken") %>';
        
        function clearMessages() {
            $('#result-message').empty();
            $('#customer-details').empty();
            $('#customer-list').empty();
        }
        
        function showLoading(message) {
            return '<div class="info-message"><h3>' + message + '</h3></div>';
        }

        // View Customer
        $('#viewCustomerForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#customer-details').html(showLoading('Loading customer details...'));
            
            $.ajax({
                url: 'customer',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#customer-details').empty();
                    if (response.success) {
                        $('#customer-details').html(
                            '<div class="customer-details">' +
                            '<h2>Customer Details</h2>' +
                            '<div class="customer-info">' + 
                            response.data.replace(/\n/g, '<br>') + 
                            '</div>' +
                            '</div>'
                        );
                    } else {
                        $('#customer-details').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#customer-details').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        
        // View Customers by Branch
        $('#viewCustomersByBranchForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#customer-list').html(showLoading('Loading branch customers...'));
            
            $.ajax({
                url: 'customer',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#customer-list').empty();
                    if (response.success) {
                        var customersHtml = '<div class="customers-list"><h2>Branch Customers</h2>';
                        if (Array.isArray(response.data)) {
                            response.data.forEach(function(customer) {
                                customersHtml += '<div class="customer-item">' + customer.replace(/\n/g, '<br>') + '</div><hr>';
                            });
                        } else {
                            customersHtml += '<div class="customer-item">' + response.data.replace(/\n/g, '<br>') + '</div>';
                        }
                        customersHtml += '</div>';
                        $('#customer-list').html(customersHtml);
                    } else {
                        $('#customer-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#customer-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Update Customer
        $('#updateCustomerForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#result-message').html(showLoading('Updating customer...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'customer',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#result-message').empty();
                    if (response.success) {
                        $('#result-message').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        $('#updateCustomerForm')[0].reset();
                    } else {
                        $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Delete Customer
        $('#deleteCustomerForm').on('submit', function(e) {
            e.preventDefault();
            
            if (confirm('⚠️ Are you sure you want to delete this customer?\n\nThis action cannot be undone and will fail if the customer has active accounts.')) {
                clearMessages();
                $('#result-message').html(showLoading('Deleting customer...'));
                
                var formData = $(this).serialize();
                if (formData.indexOf('csrfToken') === -1) {
                    formData += '&csrfToken=' + encodeURIComponent(csrfToken);
                }
                
                $.ajax({
                    url: 'customer',
                    type: 'POST',
                    data: formData,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    success: function(response) {
                        $('#result-message').empty();
                        if (response.success) {
                            $('#result-message').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                            $('#deleteCustomerForm')[0].reset();
                        } else {
                            $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                        }
                    },
                    error: function(xhr, status, error) {
                        $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                    }
                });
            }
        });
        
        
    });
    </script>
</body>
</html>