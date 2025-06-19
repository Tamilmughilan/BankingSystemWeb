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
        
        <nav class="top-navbar">
            <div class="nav-brand">
                <h2>Banking System</h2>
            </div>
            <div class="nav-links">
                <a href="index.jsp" class="nav-button">Home</a>
                <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
                    <a href="customer.jsp" class="nav-button">Customer Management</a>
                <% } %>
                <a href="account.jsp" class="nav-button active">Account Management</a>
                <a href="profile.jsp" class="nav-button user-profile">
                    <%= session.getAttribute("userName") %> (<%= session.getAttribute("role") %>)
                </a>
                <a href="logout" class="nav-button logout">Logout</a>
            </div>
        </nav>

        <!-- Page Header -->
        <div class="page-header">
            <h1>Account Management</h1>
        </div>
        
       
		<div class="storage-selection">
		    <h3>Select Storage Type</h3>
		    <div class="storage-options">
		        <label class="storage-option">
		            <input type="radio" name="storageType" value="database" checked>
		            <span>MySQL Database</span>
		        </label>
		        <label class="storage-option">
		            <input type="radio" name="storageType" value="mongodb">
		            <span>MongoDB</span>
		        </label>
		        <label class="storage-option">
		            <input type="radio" name="storageType" value="collection">
		            <span>In-Memory Collection</span>
		        </label>
		    </div>
		</div>

        <!-- Operation Buttons -->
        <div class="operation-buttons">
<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
<button data-section="create-account" class="operation-btn">Create Account</button>
<% } %>
<button data-section="transactions" class="operation-btn">Transactions</button>
<button data-section="view-accounts" class="operation-btn">View Accounts</button>
<% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
<button data-section="joint-accounts" class="operation-btn">Joint Accounts</button>
<% } %>
</div>

        <!-- Dynamic Result Areas -->
        <div id="result-message"></div>
        <div id="account-details"></div>
        <div id="account-list"></div>
        <div id="transaction-result"></div>

        <!-- Server-side Messages -->
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="success-message">
                <h3><%= request.getAttribute("successMessage") %></h3>
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
                <h3><%= request.getAttribute("errorMessage") %></h3>
            </div>
        <% } %>

        <!-- Create Account Section -->
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div id="create-account" class="content-section">
            <h3>Create New Savings Account</h3>
            <form id="createAccountForm" action="account" method="post" class="form-container">
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
                
                <button type="submit" class="submit-btn">Create Account</button>
            </form>
        </div>
        <% } %>

        <!-- Transactions Section -->
        <div id="transactions" class="content-section">
            <h3>Account Transactions</h3>
            
            <div class="transaction-tabs">
    <button class="tab-btn active">Deposit</button>
    <button class="tab-btn">Withdraw</button>
</div>

            <!-- Deposit Form -->
            <div id="deposit" class="tab-content active">
                <h4>Deposit Money</h4>
                <form id="depositForm" action="account" method="post" class="form-container">
                    <input type="hidden" name="action" value="deposit">
                    <input type="hidden" name="storageType" value="database">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="depositAccountNo">Account Number:</label>
                            <input type="number" id="depositAccountNo" name="accountNo" required>
                        </div>
                        <div class="form-group">
                            <label for="depositAmount">Amount:</label>
                            <input type="number" id="depositAmount" name="amount" step="0.01" min="1" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-btn">Deposit</button>
                </form>
            </div>

            <!-- Withdraw Form -->
            <div id="withdraw" class="tab-content">
                <h4>Withdraw Money</h4>
                <form id="withdrawForm" action="account" method="post" class="form-container">
                    <input type="hidden" name="action" value="withdraw">
                    <input type="hidden" name="storageType" value="database">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="withdrawAccountNo">Account Number:</label>
                            <input type="number" id="withdrawAccountNo" name="accountNo" required>
                        </div>
                        <div class="form-group">
                            <label for="withdrawAmount">Amount:</label>
                            <input type="number" id="withdrawAmount" name="amount" step="0.01" min="1" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-btn">Withdraw</button>
                </form>
            </div>
        </div>

        <!-- View Accounts Section -->
        <div id="view-accounts" class="content-section">
            <h3>View Accounts</h3>
            
            <div class="view-tabs">
    <button class="tab-btn active">Single Account</button>
    <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <button class="tab-btn">By Branch</button>
        <button class="tab-btn">By Customer</button>
    <% } %>
</div>

            <!-- Single Account View -->
            <div id="single" class="tab-content active">
                <h4>View Account Details</h4>
                <form id="viewAccountForm" action="account" method="get" class="form-container">
                    <input type="hidden" name="action" value="get">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-group">
                        <label for="accountNo">Account Number:</label>
                        <input type="number" id="accountNo" name="accountNo" required>
                    </div>
                    
                    <button type="submit" class="submit-btn">View Account</button>
                </form>
            </div>

            <!-- Branch View -->
            <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
            <div id="branch" class="tab-content">
                <h4>View Accounts by Branch</h4>
                <form id="viewAccountsByBranchForm" action="account" method="get" class="form-container">
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
                    
                    <button type="submit" class="submit-btn">View Branch Accounts</button>
                </form>
            </div>

            <!-- Customer View -->
            <div id="customer" class="tab-content">
                <h4>View Accounts by Customer</h4>
                <form id="viewAccountsByCustomerForm" action="account" method="get" class="form-container">
                    <input type="hidden" name="action" value="getByCustomer">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-group">
                        <label for="customerIdFilter">Customer ID:</label>
                        <input type="number" id="customerIdFilter" name="customerId" required>
                    </div>
                    
                    <button type="submit" class="submit-btn">View Customer Accounts</button>
                </form>
            </div>
            <% } %>
        </div>

        <!-- Joint Accounts Section -->
        <% if ("MANAGER".equals(session.getAttribute("role")) || "EMPLOYEE".equals(session.getAttribute("role"))) { %>
        <div id="joint-accounts" class="content-section">
            <h3>Joint Account Operations</h3>
            
            <div class="joint-tabs">
    <button class="tab-btn active">Create Joint Account</button>
    <button class="tab-btn">Add Holder</button>
    <button class="tab-btn">Remove Holder</button>
    <button class="tab-btn">View Holders</button>
</div>

            <!-- Create Joint Account -->
            <div id="create" class="tab-content active">
                <h4>Create Joint Account</h4>
                <form id="createJointAccountForm" action="account" method="post" class="form-container">
                    <input type="hidden" name="action" value="createJoint">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-group">
                        <label for="customerIds">Customer IDs (comma-separated):</label>
                        <input type="text" id="customerIds" name="customerIds" 
                               pattern="\d+(,\s*\d+)+" 
                               title="Enter at least two numeric IDs separated by commas"
                               placeholder="e.g., 1, 2, 3"
                               required>
                    </div>

                    <div class="form-row">
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
                    </div>
                    
                    <button type="submit" class="submit-btn">Create Joint Account</button>
                </form>
            </div>

            <!-- Add Joint Holder -->
            <div id="add" class="tab-content">
                <h4>Add Joint Holder</h4>
                <form id="addJointHolderForm" action="account" method="post" class="form-container">
                    <input type="hidden" name="action" value="addJointHolder">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="accountNoToAdd">Account Number:</label>
                            <input type="number" id="accountNoToAdd" name="accountNo" required>
                        </div>
                        <div class="form-group">
                            <label for="customerIdToAdd">Customer ID:</label>
                            <input type="number" id="customerIdToAdd" name="customerId" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-btn">Add Joint Holder</button>
                </form>
            </div>

            <!-- Remove Joint Holder -->
            <div id="remove" class="tab-content">
                <h4>Remove Joint Holder</h4>
                <form id="removeJointHolderForm" action="account" method="post" class="form-container">
                    <input type="hidden" name="action" value="removeJointHolder">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="accountNoToRemove">Account Number:</label>
                            <input type="number" id="accountNoToRemove" name="accountNo" required>
                        </div>
                        <div class="form-group">
                            <label for="customerIdToRemove">Customer ID:</label>
                            <input type="number" id="customerIdToRemove" name="customerId" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-btn danger">Remove Joint Holder</button>
                </form>
            </div>

            <!-- View Joint Holders -->
            <div id="view" class="tab-content">
                <h4>View Joint Holders</h4>
                <form id="viewJointHoldersForm" action="account" method="get" class="form-container">
                    <input type="hidden" name="action" value="viewJointHolders">
                    <input type="hidden" name="storageType" value="database">
                    
                    <div class="form-group">
                        <label for="accountNoToView">Account Number:</label>
                        <input type="number" id="accountNoToView" name="accountNo" required>
                    </div>
                    
                    <button type="submit" class="submit-btn">View Joint Holders</button>
                </form>
            </div>
        </div>
        <% } %>


        <% if (request.getAttribute("showAccountDetails") != null && request.getAttribute("account") != null) { %>
            <div class="account-details">
                <h3>Account Details</h3>
                <p><strong>Storage Type:</strong> <%= request.getAttribute("storageType") %></p>
                <div class="account-info">
                    <%= request.getAttribute("account").toString().replace("\n", "<br>") %>
                </div>
            </div>
        <% } %>
        
        <% if (request.getAttribute("showAccountsList") != null && request.getAttribute("accounts") != null) { %>
            <div class="accounts-list">
                <h3>Accounts List</h3>
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
    </div>

    <script nonce="<%= request.getAttribute("cspNonce") %>">


    function updateStorageType() {
        const selectedStorage = document.querySelector('input[name="storageType"]:checked').value;
        const forms = document.querySelectorAll('form');
        
        forms.forEach(form => {
            const storageInput = form.querySelector('input[name="storageType"]');
            if (storageInput) {
                storageInput.value = selectedStorage;
            }
        });
    }

    function showSection(sectionId) {
        const sections = document.querySelectorAll('.content-section');
        sections.forEach(section => section.style.display = 'none');
        
        document.getElementById(sectionId).style.display = 'block';
        
        const buttons = document.querySelectorAll('.operation-btn');
        buttons.forEach(btn => btn.classList.remove('active'));
        event.target.classList.add('active');
    }

    function showTab(tabId) {
        const tabs = document.querySelectorAll('#transactions .tab-content');
        tabs.forEach(tab => tab.classList.remove('active'));
        
        const buttons = document.querySelectorAll('#transactions .tab-btn');
        buttons.forEach(btn => btn.classList.remove('active'));
        
        document.getElementById(tabId).classList.add('active');
        event.target.classList.add('active');
    }

    function showViewTab(tabId) {
        const tabs = document.querySelectorAll('#view-accounts .tab-content, #view-customers .tab-content');
        tabs.forEach(tab => tab.classList.remove('active'));
        
        const buttons = document.querySelectorAll('#view-accounts .tab-btn, #view-customers .tab-btn');
        buttons.forEach(btn => btn.classList.remove('active'));
        
        document.getElementById(tabId).classList.add('active');
        event.target.classList.add('active');
    }

    function showJointTab(tabId) {
        const tabs = document.querySelectorAll('#joint-accounts .tab-content');
        tabs.forEach(tab => tab.classList.remove('active'));
        
        const buttons = document.querySelectorAll('#joint-accounts .tab-btn');
        buttons.forEach(btn => btn.classList.remove('active'));
        
        document.getElementById(tabId).classList.add('active');
        event.target.classList.add('active');
    }

    document.addEventListener('DOMContentLoaded', function() {
        // Storage type radio listeners
        const storageRadios = document.querySelectorAll('input[name="storageType"]');
        storageRadios.forEach(radio => {
            radio.addEventListener('change', updateStorageType);
        });
        
        // Operation button listeners
        document.querySelectorAll('.operation-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const sectionId = this.getAttribute('data-section');
                showSection(sectionId);
            });
        });
        
        // Transaction tab button listeners - REMOVE INLINE ONCLICK
        document.querySelectorAll('#transactions .tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                if (this.textContent.trim() === 'Deposit') {
                    showTab('deposit');
                } else if (this.textContent.trim() === 'Withdraw') {
                    showTab('withdraw');
                }
            });
        });
        
        // View tab button listeners - REMOVE INLINE ONCLICK
        document.querySelectorAll('#view-accounts .tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const buttonText = this.textContent.trim();
                if (buttonText === 'Single Account') {
                    showViewTab('single');
                } else if (buttonText === 'By Branch') {
                    showViewTab('branch');
                } else if (buttonText === 'By Customer') {
                    showViewTab('customer');
                }
            });
        });
        
        // Joint account tab button listeners - REMOVE INLINE ONCLICK
        document.querySelectorAll('#joint-accounts .tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const buttonText = this.textContent.trim();
                if (buttonText === 'Create Joint Account') {
                    showJointTab('create');
                } else if (buttonText === 'Add Holder') {
                    showJointTab('add');
                } else if (buttonText === 'Remove Holder') {
                    showJointTab('remove');
                } else if (buttonText === 'View Holders') {
                    showJointTab('view');
                }
            });
        });
    });
   
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
        
 
        $.ajaxSetup({
            beforeSend: function(xhr, settings) {
                if (settings.type === 'POST') {
                    xhr.setRequestHeader('X-CSRF-Token', csrfToken);
                    xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                }
            }
        });

        //Create Account
        $('#createAccountForm').on('submit', function(e) {
            e.preventDefault();
            clearMessages();
            $('#result-message').html(showLoading('Creating account...'));
            
            var selectedStorage = $('input[name="storageType"]:checked').val();
            var formData = $(this).serialize().replace(/storageType=[^&]*/, 'storageType=' + selectedStorage);
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