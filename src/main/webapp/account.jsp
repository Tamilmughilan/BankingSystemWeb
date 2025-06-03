<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Account</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Welcome, <%= session.getAttribute("userName") %></h1>
            <div class="logout-container">
                <a href="logout" class="button logout-btn">Logout</a>
            </div>
        </div>
        
        <!-- Message containers for AJAX responses -->
        <div id="result-message"></div>
		<div id="account-details"></div>
		<div id="transaction-result"></div>
        
        <!-- Server-side messages (for non-AJAX requests) -->
        <% if (request.getAttribute("successMessage") != null){ %>
        	<div class="success-message">
        		<h2><%= request.getAttribute("successMessage") %></h2>
        		<% if (request.getAttribute("showCreateResult") != null && request.getAttribute("newAccountNo") != null) {%>
        		<p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                    <p>Account Number: <%= request.getAttribute("newAccountNo") %></p>
                <% } else if (request.getAttribute("showWithdrawResult") != null || request.getAttribute("showDepositResult") != null) { %>
                    <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
        		<%} %>
        	</div>
        <%} %>
        
        <% if (request.getAttribute("errorMessage") != null){ %>
        	<div class="error-message">
        		<h2><%= request.getAttribute("errorMessage") %></h2>
        	</div>
        <%} %>
        
        <% if (request.getAttribute("showAccountDetails") != null) { %>
            <div class="account-details">
                <h2>Account Details</h2>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <% if (request.getAttribute("account") != null) { %>
                    <div class="account-info">
                        <%= request.getAttribute("account").toString().replace("\n", "<br>") %>
                    </div>
                <% } else { %>
                    <h2>Account not found</h2>
                <% } %>
            </div>
        <% } %>

        <div class="section">
            <h2>Create New Savings Account</h2>
            <form id="createAccountForm" action="account" method="post">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="storageType" value="database">
                <div class="form-group">
                    <label for="customerId">Customer ID:</label>
                    <input type="number" id="customerId" name="customerId" required>
                </div>
                <div class="form-group">
                    <label for="balance">Initial Balance:</label>
                    <input type="number" id="balance" name="balance" step="0.01" min="100" required>
                </div>
                <div class="form-group">
                    <label for="branchId">Branch ID:</label>
                    <select id="branchId" name="branchId" required>
                        <option value="">Select Branch</option>
                        <option value="1">Main</option>
                        <option value="2">Anna nagar</option>
                        <option value="3">Adyar</option>
                    </select>
                </div>
                <input type="submit" value="Create Savings Account" class="button">
            </form>
        </div>

        <div class="section">
            <h2>View Account Details</h2>
            <form id="viewAccountForm" action="account" method="get">
                <input type="hidden" name="action" value="get">
                <input type="hidden" name="storageType" value="database">
                <div class="form-group">
                    <label for="accountNo">Account Number:</label>
                    <input type="number" id="accountNo" name="accountNo" required placeholder="Enter account number">
                </div>
                <input type="submit" value="View Account" class="button">
            </form>
        </div>

        <div class="section">
            <h2>Deposit Money</h2>
            <form id="depositForm" action="account" method="post">
                <input type="hidden" name="action" value="deposit">
                <input type="hidden" name="storageType" value="database">
                <div class="form-group">
                    <label for="depositAccountNo">Account Number:</label>
                    <input type="number" id="depositAccountNo" name="accountNo" required placeholder="Enter account number">
                </div>
                <div class="form-group">
                    <label for="depositAmount">Amount:</label>
                    <input type="number" id="depositAmount" name="amount" step="0.01" min="1" required placeholder="Enter amount to deposit">
                </div>
                <input type="submit" value="Deposit" class="button deposit-btn">
            </form>
        </div>

        <div class="section">
            <h2>Withdraw Money</h2>
            <form id="withdrawForm" action="account" method="post">
                <input type="hidden" name="action" value="withdraw">
                <input type="hidden" name="storageType" value="database">
                <div class="form-group">
                    <label for="withdrawAccountNo">Account Number:</label>
                    <input type="number" id="withdrawAccountNo" name="accountNo" required placeholder="Enter account number">
                </div>
                <div class="form-group">
                    <label for="withdrawAmount">Amount:</label>
                    <input type="number" id="withdrawAmount" name="amount" step="0.01" min="1" required placeholder="Enter amount to withdraw">
                </div>
                <input type="submit" value="Withdraw" class="button withdraw-btn">
            </form>
        </div>
    </div>
    
    <script>
    $(document).ready(function(){
        
        //Clearing previous messages 
        function clearMessages() {
            $('#result-message').empty();
            $('#account-details').empty();
            $('#transaction-result').empty();
        }
        
        // For creating account
        $('#createAccountForm').on('submit', function(e){
            e.preventDefault();
            clearMessages();
            
            
            $('#result-message').html('<div class="info-message"><h3>Processing...</h3></div>');
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: $(this).serialize(),
                headers:{
                    'X-Requested-With' : 'XMLHttpRequest'
                },
                success: function(response){
                    console.log('Create Account Response:', response); // Debug log
                    $('#result-message').empty();
                    
                    if (response.success) {
                        $('#result-message').html(
                            '<div class="success-message">' +
                            '<h2>' + response.message + '</h2>' + 
                            (response.data ? '<p><strong>New Account Number:</strong> ' + response.data + '</p>' : '') + 
                            '</div>'
                        );
                        // Reset form after success
                        $('#createAccountForm')[0].reset();
                    } else {
                        $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }        
                },
                error: function(xhr, status, error){
                    console.log('Create Account Error:', xhr.responseText); // Debug log
                    $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });
        
        //For Viewing Account 
        $('#viewAccountForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            
            //Show loading message
            $('#account-details').html('<div class="info-message"><h3>Loading account details...</h3></div>');
            
            $.ajax({
                url: 'account',
                type: 'GET',
                data: $(this).serialize(),
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    console.log('View Account Response:', response); // Debug log
                    $('#account-details').empty();
                    
                    if (response.success) {
                        $('#account-details').html(
                            '<div class="account-details">' +
                            '<h2>Account Details</h2>' +
                            '<div class="account-info">' + 
                            response.data.replace(/\n/g, '<br>') + 
                            '</div>' +
                            '</div>'
                        );
                    } else {
                        $('#account-details').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    console.log('View Account Error:', xhr.responseText); // Debug log
                    $('#account-details').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });
        
        // Deposit Form
        $('#depositForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            
            // Show loading message
            $('#transaction-result').html('<div class="info-message"><h3>Processing deposit...</h3></div>');
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: $(this).serialize(),
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    console.log('Deposit Response:', response); // Debug log
                    $('#transaction-result').empty();
                    
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        // Reset form after success
                        $('#depositForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    console.log('Deposit Error:', xhr.responseText); // Debug log
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Withdraw Form
        $('#withdrawForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            
            // Show loading message
            $('#transaction-result').html('<div class="info-message"><h3>Processing withdrawal...</h3></div>');
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: $(this).serialize(),
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    console.log('Withdraw Response:', response); // Debug log
                    $('#transaction-result').empty();
                    
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        // Reset form after success
                        $('#withdrawForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    console.log('Withdraw Error:', xhr.responseText); // Debug log
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });
    });
    </script>
</body>
</html>