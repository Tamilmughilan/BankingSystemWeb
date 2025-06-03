<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <title>Customer Management</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <h1>Customer Management</h1>
		 <p>Hello, <%= session.getAttribute("userName") %>! (Role: <%= session.getAttribute("role") %>)</p>
        
        <!-- Server-side messages (for non-AJAX requests) -->
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="success-message">
                <h2><%= request.getAttribute("successMessage") %></h2>
            </div>
        <% } %>

        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="error-message">
                <h2><%= request.getAttribute("errorMessage") %></h2>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showCustomerDetails") != null) { %>
            <div class="customer-details">
                <% if (request.getAttribute("customer") != null) { %>
                    <h2>Customer Details</h2>
                    <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                    <div class="customer-info">
                        <%= request.getAttribute("customer").toString().replace("\n", "<br>") %>
                    </div>
                <% } else { %>
                    <h2>Customer Not Found</h2>
                <% } %>
            </div>
        <% } %>
        
        <!-- AJAX message container -->
        <div id="customer-result"></div>
        
        <div class="section">
            <h2>Get Customer</h2>
            <form id="getCustomerForm" action="customer" method="get">
                <input type="hidden" name="action" value="get">
                <p>Storage Type: 
                    <select name="storageType" required>
                        <option value="database">Database Storage</option>
                        <option value="collection">Collection Storage</option>
                    </select>
                </p>
                <p>Customer ID: <input type="number" name="customerId" required></p>
                <p><input type="submit" value="Get Customer" class="button"></p>
            </form>
        </div>
        
        <div class="section">
            <h2>Update Customer</h2>
            <form id="updateCustomerForm" action="customer" method="post">
                <input type="hidden" name="action" value="update">
                <p>Storage Type: 
                    <select name="storageType" required>
                        <option value="database">Database Storage</option>
                    </select>
                </p>
                <p>Customer ID: <input type="number" name="customerId" required></p>
                <p>Name: <input type="text" name="name" required></p>
                <p>Phone: <input type="text" name="phone" required></p>
                <p>Email: <input type="email" name="email" required></p>
                <p>Branch ID: <input type="number" name="branchId" required></p>
                <p><input type="submit" value="Update Customer" class="button"></p>
            </form>
        </div>
        
        <div class="section">
            <h2>Delete Customer</h2>
            <form id="deleteCustomerForm" action="customer" method="post">
                <input type="hidden" name="action" value="delete">
                <p>Storage Type: 
                    <select name="storageType" required>
                        <option value="database">Database Storage</option>
                    </select>
                </p>
                <p>Customer ID: <input type="number" name="customerId" required></p>
                <p><input type="submit" value="Delete Customer" class="button danger"></p>
            </form>
        </div>
        
        <div class="menu">
            <a href="index.jsp" class="button">Home</a>
            <a href="logout" class="button">Logout</a>
        </div>
    </div>
    
    <script>
    $(document).ready(function() {
        
        // Clear previous messages function
        function clearMessages() {
            $('#customer-result').empty();
        }
        
        // Get Customer AJAX 
        $('#getCustomerForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            
            // Show loading message
            $('#customer-result').html('<div class="info-message"><h3>Loading customer details...</h3></div>');
            
            $.ajax({
                url: 'customer',
                type: 'GET',
                data: $(this).serialize(),
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    console.log('Get Customer Response:', response); // Debug log
                    $('#customer-result').empty();
                    
                    if (response.success) {
                        $('#customer-result').html(
                            '<div class="customer-details">' +
                            '<h2>Customer Details</h2>' +
                            '<div class="customer-info">' + 
                            response.data.replace(/\n/g, '<br>') + 
                            '</div>' +
                            '</div>'
                        );
                    } else {
                        $('#customer-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    console.log('Get Customer Error:', xhr.responseText); // Debug log
                    $('#customer-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Update Customer AJAX 
        $('#updateCustomerForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            
            // Show loading message
            $('#customer-result').html('<div class="info-message"><h3>Updating customer...</h3></div>');
            
            $.ajax({
                url: 'customer',
                type: 'POST',
                data: $(this).serialize(),
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    console.log('Update Customer Response:', response); // Debug log
                    $('#customer-result').empty();
                    
                    if (response.success) {
                        $('#customer-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        // Reset form after success
                        $('#updateCustomerForm')[0].reset();
                    } else {
                        $('#customer-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    console.log('Update Customer Error:', xhr.responseText); // Debug log
                    $('#customer-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Delete Customer AJAX 
        $('#deleteCustomerForm').on('submit', function(e) {
            e.preventDefault();
            
            if (confirm('Are you sure you want to delete this customer?')) {
                clearMessages();
                
                // Show loading message
                $('#customer-result').html('<div class="info-message"><h3>Deleting customer...</h3></div>');
                
                $.ajax({
                    url: 'customer',
                    type: 'POST',
                    data: $(this).serialize(),
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    success: function(response) {
                        console.log('Delete Customer Response:', response); // Debug log
                        $('#customer-result').empty();
                        
                        if (response.success) {
                            $('#customer-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                            // Reset form after success
                            $('#deleteCustomerForm')[0].reset();
                        } else {
                            $('#customer-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                        }
                    },
                    error: function(xhr, status, error) {
                        console.log('Delete Customer Error:', xhr.responseText); // Debug log
                        $('#customer-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                    }
                });
            }
        });
    });
	</script>
</body>
</html>