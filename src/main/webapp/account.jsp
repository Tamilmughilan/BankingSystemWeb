<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Account Management</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
    <div class="container">
       
        <div class="header">
            <h1>Account Management</h1>
            <div class="nav-container">
                <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
                    <a href="customer.jsp" class="button">Customer Management</a>
                <% } %>
                <a href="index.jsp" class="button">Home</a>
                <a href="logout" class="button logout-btn">Logout</a>
            </div>
        </div>
        
        <p class="welcome-text">Welcome <%= session.getAttribute("userName") %>! (Role: <%= session.getAttribute("role") %>)</p>
        
        
        <div id="result-message"></div>
        <div id="account-details"></div>
        <div id="account-list"></div>
        <div id="transaction-result"></div>
        
        
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="success-message">
                <h2><%= request.getAttribute("successMessage") %></h2>
                <% if (request.getAttribute("showCreateResult") != null && request.getAttribute("newAccountNo") != null) { %>
                    <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                    <p><strong>New Account Number:</strong> <%= request.getAttribute("newAccountNo") %></p>
                <% } else if (request.getAttribute("showWithdrawResult") != null || request.getAttribute("showDepositResult") != null) { %>
                    <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <% } %>
            </div>
        <% } %>
        
        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="error-message">
                <h2><%= request.getAttribute("errorMessage") %></h2>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showAccountDetails") != null && request.getAttribute("account") != null) { %>
            <div class="account-details">
                <h2>Account Details</h2>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <div class="account-info">
                    <%= request.getAttribute("account").toString().replace("\n", "<br>") %>
                </div>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showAccountsList") != null && request.getAttribute("accounts") != null) { %>
            <div class="accounts-list">
                <h2>Accounts List</h2>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <div class="accounts-info">
                    <% 
                    java.util.List<?> accounts = (java.util.List<?>) request.getAttribute("accounts");
                    for (Object account : accounts) {
                    %>
                        <div class="account-item">
                            <%= account.toString().replace("\n", "<br>") %>
                        </div>
                        <hr>
                    <% } %>
                </div>
            </div>
        <% } %>
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>Create New Savings Account</h3>
            <form id="createAccountForm" action="account" method="post">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="customerId">Customer ID:</label>
                        <input type="number" id="customerId" name="customerId" required>
                    </div>
                    <div class="form-group">
                        <label for="balance">Initial Balance:</label>
                        <input type="number" id="balance" name="balance" step="0.01" min="100" required>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="branchId">Branch:</label>
                    <select id="branchId" name="branchId" required>
                        <option value="">Select Branch</option>
                        <option value="1">Main Branch</option>
                        <option value="2">Anna Nagar Branch</option>
                        <option value="3">Adyar Branch</option>
                    </select>
                </div>
                
                <input type="submit" value="Create Account" class="button submit-btn">
            </form>
        </div>
        <% } %>

       
        <div class="section">
            <h3>View Account Details</h3>
            <form id="viewAccountForm" action="account" method="get">
                <input type="hidden" name="action" value="get">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="accountNo">Account Number:</label>
                    <input type="number" id="accountNo" name="accountNo" required placeholder="Enter account number">
                </div>
                
                <input type="submit" value="View Account" class="button info-btn">
            </form>
        </div>

        
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>View Accounts by Branch</h3>
            <form id="viewAccountsByBranchForm" action="account" method="get">
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
                
                <input type="submit" value="View Branch Accounts" class="button info-btn">
            </form>
        </div>
        <% } %>

        
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div class="section">
            <h3>View Accounts by Customer</h3>
            <form id="viewAccountsByCustomerForm" action="account" method="get">
                <input type="hidden" name="action" value="getByCustomer">
                <input type="hidden" name="storageType" value="database">
                
                <div class="form-group">
                    <label for="customerIdFilter">Customer ID:</label>
                    <input type="number" id="customerIdFilter" name="customerId" required placeholder="Enter customer ID">
                </div>
                
                <input type="submit" value="View Customer Accounts" class="button info-btn">
            </form>
        </div>
        <% } %>

        
        <div class="transaction-section">
            <h3>Account Transactions</h3>
            
            
            <div class="section">
                <h4>Deposit Money</h4>
                <form id="depositForm" action="account" method="post">
                    <input type="hidden" name="action" value="deposit">
                    <input type="hidden" name="storageType" value="database">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="depositAccountNo">Account Number:</label>
                            <input type="number" id="depositAccountNo" name="accountNo" required placeholder="Enter account number">
                        </div>
                        <div class="form-group">
                            <label for="depositAmount">Amount:</label>
                            <input type="number" id="depositAmount" name="amount" step="0.01" min="1" required placeholder="Enter amount">
                        </div>
                    </div>
                    
                    <input type="submit" value="Deposit" class="button deposit-btn">
                </form>
            </div>

            <!-- Withdraw Money -->
            <div class="section">
                <h4>Withdraw Money</h4>
                <form id="withdrawForm" action="account" method="post">
                    <input type="hidden" name="action" value="withdraw">
                    <input type="hidden" name="storageType" value="database">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="withdrawAccountNo">Account Number:</label>
                            <input type="number" id="withdrawAccountNo" name="accountNo" required placeholder="Enter account number">
                        </div>
                        <div class="form-group">
                            <label for="withdrawAmount">Amount:</label>
                            <input type="number" id="withdrawAmount" name="amount" step="0.01" min="1" required placeholder="Enter amount">
                        </div>
                    </div>
                    
                    <input type="submit" value="Withdraw" class="button withdraw-btn">
                </form>
            </div>
        </div>
        

<form id="createJointAccountForm" action="account" method="post">
    <input type="hidden" name="action" value="createJoint">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="storageType" value="database">
    <div class="form-group">
    <label for="customerIds">Customer IDs (comma-separated):</label>
    <input type="text" id="customerIds" name="customerIds" 
           pattern="\d+(,\s*\d+)+" 
           title="Enter at least two numeric IDs separated by commas"
           required>
</div>

    <div class="form-group">
        <label for="jointBalance">Initial Balance:</label>
        <input type="number" id="jointBalance" name="balance" step="0.01" min="100" required>
    </div>
    <div class="form-group">
        <label for="jointBranchId">Branch:</label>
        <select id="jointBranchId" name="branchId" required>
            <option value="">Select Branch</option>
            <option value="1">Main Branch</option>
            <option value="2">Anna Nagar Branch</option>
            <option value="3">Adyar Branch</option>
        </select>
    </div>
    <input type="submit" value="Create Joint Account" class="button submit-btn">
</form>

<!-- Add Joint Holder -->
<form id="addJointHolderForm" action="account" method="post">
    <input type="hidden" name="action" value="addJointHolder">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="storageType" value="database">
    <div class="form-group">
        <label for="accountNoToAdd">Account Number:</label>
        <input type="number" id="accountNoToAdd" name="accountNo" required>
    </div>
    <div class="form-group">
        <label for="customerIdToAdd">Customer ID:</label>
        <input type="number" id="customerIdToAdd" name="customerId" required>
    </div>
    <input type="submit" value="Add Joint Holder" class="button submit-btn">
</form>

<!-- Remove Joint Holder -->
<form id="removeJointHolderForm" action="account" method="post">
    <input type="hidden" name="action" value="removeJointHolder">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="storageType" value="database">
    <div class="form-group">
        <label for="accountNoToRemove">Account Number:</label>
        <input type="number" id="accountNoToRemove" name="accountNo" required>
    </div>
    <div class="form-group">
        <label for="customerIdToRemove">Customer ID:</label>
        <input type="number" id="customerIdToRemove" name="customerId" required>
    </div>
    <input type="submit" value="Remove Joint Holder" class="button danger-btn">
</form>


<form id="viewJointHoldersForm" action="account" method="get">
    <input type="hidden" name="action" value="viewJointHolders">
    <input type="hidden" name="storageType" value="database">
    <div class="form-group">
        <label for="accountNoToView">Account Number:</label>
        <input type="number" id="accountNoToView" name="accountNo" required>
    </div>
    <input type="submit" value="View Joint Holders" class="button info-btn">
</form>
        
    </div>
    
    <script>
    $(document).ready(function() {
        var csrfToken = '<%= session.getAttribute("csrfToken") %>';
        
        function clearMessages() {
            $('#result-message').empty();
            $('#account-details').empty();
            $('#account-list').empty();
            $('#transaction-result').empty();
        }
        
        function showLoading(message) {
            return '<div class="info-message"><h3>' + message + '</h3></div>';
        }

        // Create Account
        $('#createAccountForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#result-message').html(showLoading('Creating account...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#result-message').empty();
                    if (response.success) {
                        $('#result-message').html(
                            '<div class="success-message">' +
                            '<h2>' + response.message + '</h2>' + 
                            (response.data ? '<p><strong>New Account Number:</strong> ' + response.data + '</p>' : '') + 
                            '</div>'
                        );
                        $('#createAccountForm')[0].reset();
                    } else {
                        $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }        
                },
                error: function(xhr, status, error) {
                    $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // View Account
        $('#viewAccountForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#account-details').html(showLoading('Loading account details...'));
            
            $.ajax({
                url: 'account',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
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
                    $('#account-details').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // View Accounts by Branch
        $('#viewAccountsByBranchForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#account-list').html(showLoading('Loading branch accounts...'));
            
            $.ajax({
                url: 'account',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#account-list').empty();
                    if (response.success) {
                        var accountsHtml = '<div class="accounts-list"><h2>Branch Accounts</h2>';
                        if (Array.isArray(response.data)) {
                            response.data.forEach(function(account) {
                                accountsHtml += '<div class="account-item">' + account.replace(/\n/g, '<br>') + '</div><hr>';
                            });
                        } else {
                            accountsHtml += '<div class="account-item">' + response.data.replace(/\n/g, '<br>') + '</div>';
                        }
                        accountsHtml += '</div>';
                        $('#account-list').html(accountsHtml);
                    } else {
                        $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // View Accounts by Customer
        $('#viewAccountsByCustomerForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#account-list').html(showLoading('Loading customer accounts...'));
            
            $.ajax({
                url: 'account',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#account-list').empty();
                    if (response.success) {
                        var accountsHtml = '<div class="accounts-list"><h2>Customer Accounts</h2>';
                        if (Array.isArray(response.data)) {
                            response.data.forEach(function(account) {
                                accountsHtml += '<div class="account-item">' + account.replace(/\n/g, '<br>') + '</div><hr>';
                            });
                        } else {
                            accountsHtml += '<div class="account-item">' + response.data.replace(/\n/g, '<br>') + '</div>';
                        }
                        accountsHtml += '</div>';
                        $('#account-list').html(accountsHtml);
                    } else {
                        $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Deposit
        $('#depositForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#transaction-result').html(showLoading('Processing deposit...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#transaction-result').empty();
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        $('#depositForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        // Withdraw
        $('#withdrawForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#transaction-result').html(showLoading('Processing withdrawal...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#transaction-result').empty();
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        $('#withdrawForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });
     
        $('#createJointAccountForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#result-message').html(showLoading('Creating joint account...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#result-message').empty();
                    if (response.success) {
                        $('#result-message').html(
                            '<div class="success-message">' +
                            '<h2>' + response.message + '</h2>' + 
                            (response.data ? '<p><strong>New Account Number:</strong> ' + response.data + '</p>' : '') + 
                            '</div>'
                        );
                        $('#createJointAccountForm')[0].reset();
                    } else {
                        $('#result-message').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#result-message').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        $('#addJointHolderForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#transaction-result').html(showLoading('Adding joint holder...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#transaction-result').empty();
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        $('#addJointHolderForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        $('#removeJointHolderForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#transaction-result').html(showLoading('Removing joint holder...'));
            
            var formData = $(this).serialize();
            if (formData.indexOf('csrfToken') === -1) {
                formData += '&csrfToken=' + encodeURIComponent(csrfToken);
            }
            
            $.ajax({
                url: 'account',
                type: 'POST',
                data: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#transaction-result').empty();
                    if (response.success) {
                        $('#transaction-result').html('<div class="success-message"><h2>' + response.message + '</h2></div>');
                        $('#removeJointHolderForm')[0].reset();
                    } else {
                        $('#transaction-result').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#transaction-result').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        $('#viewJointHoldersForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#account-list').html(showLoading('Loading account holders...'));
            
            $.ajax({
                url: 'account',
                type: 'GET',
                data: $(this).serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(response) {
                    $('#account-list').empty();
                    if (response.success) {
                        var holdersHtml = '<div class="accounts-list"><h2>Account Holders</h2>';
                        if (Array.isArray(response.data)) {
                            holdersHtml += '<ul class="holder-list">';
                            response.data.forEach(function(holderString) {
                                // Parse the string format "{name=sidharth, id=15}"
                                var nameMatch = holderString.match(/name=([^,}]+)/);
                                var idMatch = holderString.match(/id=([^,}]+)/);
                                
                                var name = nameMatch ? nameMatch[1] : 'Unknown';
                                var id = idMatch ? idMatch[1] : 'Unknown';
                                
                                holdersHtml += '<li>' + name + ' (ID: ' + id + ')</li>';
                            });
                            holdersHtml += '</ul>';
                        } else {
                            holdersHtml += '<div class="account-item">' + response.data + '</div>';
                        }
                        holdersHtml += '</div>';
                        $('#account-list').html(holdersHtml);
                    } else {
                        $('#account-list').html('<div class="error-message"><h2>' + response.message + '</h2></div>');
                    }
                },
                error: function(xhr, status, error) {
                    $('#account-list').html('<div class="error-message"><h2>Request failed: ' + error + '</h2></div>');
                }
            });
        });

        
    });
    </script>
</body>
</html>