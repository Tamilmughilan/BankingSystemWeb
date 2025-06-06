<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customer Management</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <!-- Header Section -->
        <div class="header">
            <h1>Customer Management</h1>
            <div class="nav-container">
                <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
                    <a href="account.jsp" class="button">Account Management</a>
                <% } %>
                <a href="index.jsp" class="button">Home</a>
                <a href="logout" class="button logout-btn">Logout</a>
            </div>
        </div>
        
        <p class="welcome-text">Welcome, <%= session.getAttribute("userName") %>! (Role: <%= session.getAttribute("role") %>)</p>
        
        <!-- Dynamic Result Areas -->
        <div id="result-message"></div>
        <div id="customer-details"></div>
        <div id="customer-list"></div>
        
        <!-- Server-side Messages -->
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="success-message">
                <h2><%= request.getAttribute("successMessage") %></h2>
                <% if (request.getAttribute("showUpdateResult") != null || request.getAttribute("showDeleteResult") != null) { %>
                    <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <% } %>
            </div>
        <% } %>
        
        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="error-message">
                <h2><%= request.getAttribute("errorMessage") %></h2>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showCustomerDetails") != null && request.getAttribute("customer") != null) { %>
            <div class="customer-details">
                <h2>Customer Details</h2>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <div class="customer-info">
                    <%= request.getAttribute("customer").toString().replace("\n", "<br>") %>
                </div>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showCustomersList") != null && request.getAttribute("customers") != null) { %>
            <div class="customers-list">
                <h2>Customers List</h2>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
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

        <!-- Storage Type Selection -->
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>Storage Type Selection</h3>
            <p>Current operations will use: <strong>Database Storage</strong></p>
            <p class="info-text">Collection storage available for testing purposes.</p>
        </div>
        <% } %>

        <!-- View Customer Details -->
        <div class="section">
            <h3>View Customer Details</h3>
            <form id="viewCustomerForm" action="customer" method="get">
                <input type="hidden" name="action" value="get">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="customerId">Customer ID:</label>
                    <input type="number" id="customerId" name="customerId" required placeholder="Enter customer ID">
                </div>
                
                <input type="submit" value="View Customer" class="button info-btn">
            </form>
        </div>

        <!-- View Customer by Email -->
        <div class="section">
            <h3>View Customer by Email</h3>
            <form id="viewCustomerByEmailForm" action="customer" method="get">
                <input type="hidden" name="action" value="getByEmail">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="customerEmail">Customer Email:</label>
                    <input type="email" id="customerEmail" name="email" required placeholder="Enter customer email">
                </div>
                
                <input type="submit" value="View Customer by Email" class="button info-btn">
            </form>
        </div>

        <!-- View Customers by Branch (Admin only) -->
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>View Customers by Branch</h3>
            <form id="viewCustomersByBranchForm" action="customer" method="get">
                <input type="hidden" name="action" value="getByBranch">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="branchIdFilter">Branch:</label>
                    <select id="branchIdFilter" name="branchId" required>
                        <option value="">Select Branch</option>
                        <option value="1">Main Branch</option>
                        <option value="2">Anna Nagar Branch</option>
                        <option value="3">Adyar Branch</option>
                    </select>
                </div>
                
                <input type="submit" value="View Branch Customers" class="button info-btn">
            </form>
        </div>
        <% } %>

        <!-- Update Customer (Admin only) -->
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>Update Customer Information</h3>
            <form id="updateCustomerForm" action="customer" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="updateCustomerId">Customer ID:</label>
                        <input type="number" id="updateCustomerId" name="customerId" required placeholder="Enter customer ID">
                    </div>
                    <div class="form-group">
                        <label for="updateName">Full Name:</label>
                        <input type="text" id="updateName" name="name" required placeholder="Enter full name">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="updatePhone">Phone Number:</label>
                        <input type="tel" id="updatePhone" name="phone" required placeholder="Enter phone number">
                    </div>
                    <div class="form-group">
                        <label for="updateEmail">Email Address:</label>
                        <input type="email" id="updateEmail" name="email" required placeholder="Enter email address">
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="updateBranchId">Branch:</label>
                    <select id="updateBranchId" name="branchId" required>
                        <option value="">Select Branch</option>
                        <option value="1">Main Branch</option>
                        <option value="2">Anna Nagar Branch</option>
                        <option value="3">Adyar Branch</option>
                    </select>
                </div>
                
                <input type="submit" value="Update Customer" class="button submit-btn">
            </form>
        </div>
        <% } %>

        <!-- Delete Customer (Manager only) -->
        <% if ("MANAGER".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>Delete Customer</h3>
            <p class="warning-text">⚠️ Warning: This action cannot be undone. Customer must not have any active accounts.</p>
            <form id="deleteCustomerForm" action="customer" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="deleteCustomerId">Customer ID:</label>
                    <input type="number" id="deleteCustomerId" name="customerId" required placeholder="Enter customer ID to delete">
                </div>
                
                <input type="submit" value="Delete Customer" class="button danger-btn">
            </form>
        </div>
        <% } %>
    </div>
    
    <script>
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

        // View Customer by Email
        $('#viewCustomerByEmailForm').on('submit', function(e) {
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