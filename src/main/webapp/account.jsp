<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Banking Account</title>
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

        <div class="section">
            <h2>Create New Savings Account</h2>
            <form action="account" method="post">
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
                        <option value="1">Main Branch</option>
                        <option value="2">North Branch</option>
                        <option value="3">South Branch</option>
                    </select>
                </div>
                <input type="submit" value="Create Savings Account" class="button">
            </form>
        </div>

        <div class="section">
            <h2>View Account Details</h2>
            <form action="account" method="get">
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
            <form action="account" method="post">
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
            <form action="account" method="post">
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
</body>
</html>
