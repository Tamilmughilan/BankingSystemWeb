package servlet;

import javax.servlet.*;



import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.AccountService;
import storage.DataStorage;
import storage.DatabaseStorage;
import storage.MongoDBStorage;
import storage.CollectionStorage;
import entity.Customer;
import entity.Employee;
import entity.SavingsAccount;
import entity.TransactionLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * Servlet that handles all account related operations.
 * Manages account creation, transactions, joint accounts, and transaction history.
 * Supports multiple storage types.
 *
 * @author TAMIL MUGHILAN
 */
@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    
	/**
     * Gets the correct data storage implementation based on storage type.
     *
     * @param storageType the type of storage (database, collections, mongoDB)
     * @return the data storage implementation
     * @throws SQLException if database connection fails
     */
	private DataStorage getDataStorage(String storageType) throws SQLException {
	    if ("database".equalsIgnoreCase(storageType)) {
	        return new DatabaseStorage();
	    } else if ("collection".equalsIgnoreCase(storageType)) {
	        return new CollectionStorage();
	    } else if ("mongodb".equalsIgnoreCase(storageType)) {
	        return new MongoDBStorage();
	    } else {
	        return new DatabaseStorage(); 
	    }
	}
	
	/**
     * Gets the current user's ID from session.
     *
     * @param request the HTTP request
     * @return the user ID or null if not found
     */
	private Integer getCurrentUserId(HttpServletRequest request) {
	    Object userIdObj = request.getSession().getAttribute("userId");
	    return userIdObj != null ? (Integer) userIdObj : null;
	}

	/**
     * Gets the current user's type from session.
     *
     * @param request the HTTP request
     * @return the user type (CUSTOMER, EMPLOYEE, MANAGER) or null
     */
	private TransactionLog.UserType getCurrentUserType(HttpServletRequest request) {
	    String role = (String) request.getSession().getAttribute("role");
	    if (role != null) {
	        switch (role.toUpperCase()) {
	            case "MANAGER":
	                return TransactionLog.UserType.MANAGER;
	            case "EMPLOYEE":
	                return TransactionLog.UserType.EMPLOYEE;
	            case "CUSTOMER":
	                return TransactionLog.UserType.CUSTOMER;
	            default:
	                return null;
	        }
	    }
	    return null;
	}
	
	/**
     * Sends JSON response for AJAX requests.
     *
     * @param response the HTTP response
     * @param success whether the operation was successful
     * @param message the response message
     * @param data the response data
     * @throws IOException if writing response fails
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(message.replace("\"", "\\\"")).append("\"");
        
        if (data != null) {
            if (data instanceof List) {
                json.append(",\"data\":[");
                List<?> list = (List<?>) data;
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(list.get(i).toString().replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
                }
                json.append("]");
            } else {
                json.append(",\"data\":\"").append(data.toString().replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
            }
        }
        
        json.append("}");
        out.print(json.toString());
        out.flush();
    }

    /**
     * Sends JSON response for account data.
     *
     * @param response the HTTP response
     * @param success whether the operation was successful
     * @param message the response message
     * @param accounts list of accounts to include in response
     * @throws IOException if writing response fails
     */
    private void sendAccountsJsonResponse(HttpServletResponse response, boolean success, String message, List<SavingsAccount> accounts) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(message.replace("\"", "\\\"")).append("\"");
        
        if (accounts != null && !accounts.isEmpty()) {
            json.append(",\"data\":[");
            for (int i = 0; i < accounts.size(); i++) {
                if (i > 0) json.append(",");
                SavingsAccount account = accounts.get(i);
                json.append("{");
                json.append("\"accountNo\":").append(account.getAccountNo()).append(",");
                json.append("\"balance\":").append(account.getBalance()).append(",");
                json.append("\"accountType\":\"").append(account.getAccountType()).append("\",");
                json.append("\"customerId\":").append(account.getCustomerId()).append(",");
                json.append("\"branchId\":").append(account.getBranchId());
                json.append("}");
            }
            json.append("]");
        }
        
        json.append("}");
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Handles GET requests for account retrieval operations.
     * Performs Dependency Injection by passing the user's choice of data storage into the Services.
     * Viewing accounts by ID, branch, customer, and transaction history.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String accountNo = request.getParameter("accountNo");
        String branchId = request.getParameter("branchId");
        String customerId = request.getParameter("customerId");
        String storageType = request.getParameter("storageType");
        
        try {
            DataStorage dataStorage = getDataStorage(storageType);
            AccountService accountService = new AccountService(dataStorage);
            
            if ("get".equals(action) && accountNo != null) {
                int accNo = Integer.parseInt(accountNo);
                SavingsAccount account = accountService.getAccount(accNo);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (account == null) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Account not found with account number: " + accNo, null);
                    } else {
                        request.setAttribute("errorMessage", "Account not found with account number: " + accNo);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Account retrieved successfully", account);
                    } else {
                        request.setAttribute("account", account);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showAccountDetails", true);
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            }
            
            
            else if ("getByBranch".equals(action) && branchId != null) {
                int brId = Integer.parseInt(branchId);
                List<SavingsAccount> accounts = accountService.getAccountsByBranch(brId);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (accounts == null || accounts.isEmpty()) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "No accounts found for branch ID: " + brId, null);
                    } else {
                        request.setAttribute("errorMessage", "No accounts found for branch ID: " + brId);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Accounts retrieved successfully for branch " + brId, accounts);
                    } else {
                        request.setAttribute("accounts", accounts);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showAccountsList", true);
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            }
            else if ("viewJointHolders".equals(action)) {
                try {
                    int accNo = Integer.parseInt(request.getParameter("accountNo"));
                    List<Customer> holders = accountService.getAccountHolders(accNo);
                    
                    // For AJAX requests, send JSON response
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        if (holders != null && !holders.isEmpty()) {
                            // Convert holders to JSON-friendly format
                            List<Map<String, Object>> holderData = new ArrayList<>();
                            for (Customer holder : holders) {
                                Map<String, Object> holderMap = new HashMap<>();
                                holderMap.put("name", holder.getName());
                                holderMap.put("id", holder.getId());
                                
                            
                                holderData.add(holderMap);
                            }
                            sendJsonResponse(response, true, "Account holders retrieved successfully", holderData);
                        } else {
                            sendJsonResponse(response, false, "No holders found for account " + accNo, null);
                        }
                        return; 
                    } else {
                    
                        request.setAttribute("accountHolders", holders);
                        request.setAttribute("queriedAccountNo", accNo);
                    }
                } catch (NumberFormatException e) {
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Invalid account number format", null);
                        return;
                    } else {
                        request.setAttribute("errorMessage", "Invalid account number format");
                    }
                } catch (Exception e) {
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Error retrieving account holders: " + e.getMessage(), null);
                        return;
                    } else {
                        request.setAttribute("errorMessage", "Error retrieving account holders: " + e.getMessage());
                    }
                }
            }
            
            else if ("getTransactionHistory".equals(action) && accountNo != null) {
                int accNo = Integer.parseInt(accountNo);
                String limitStr = request.getParameter("limit");
                int limit = limitStr != null ? Integer.parseInt(limitStr) : 50;
                
                List<TransactionLog> transactions = accountService.getTransactionHistory(accNo, limit);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (transactions == null || transactions.isEmpty()) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "No transaction history found for account: " + accNo, null);
                    } else {
                        request.setAttribute("errorMessage", "No transaction history found for account: " + accNo);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Transaction history retrieved successfully", transactions);
                    } else {
                        request.setAttribute("transactions", transactions);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showTransactionHistory", true);
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            }

            else if ("getUserAccounts".equals(action)) {
                // Get current user's accounts for dropdown
                Integer userId = getCurrentUserId(request);
                String userRole = (String) request.getSession().getAttribute("role");
                
                if (userId == null) {
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "User not logged in", null);
                    } else {
                        request.setAttribute("errorMessage", "User not logged in");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                    return;
                }
                
                try {
                    // Initialize to empty list 
                    List<SavingsAccount> userAccounts = new ArrayList<>();
                    
                    // Get accounts based on user role
                    if ("CUSTOMER".equals(userRole)) {
                        List<SavingsAccount> customerAccounts = accountService.getAccountsByCustomer(userId);
                        if (customerAccounts != null) {
                            userAccounts = customerAccounts;
                        }
                    } else if ("EMPLOYEE".equals(userRole) || "MANAGER".equals(userRole)) {
                        // For employees/managers, show all accounts in their branch
                        Employee employee = dataStorage.getEmployee(userId);
                        if (employee != null) {
                            List<SavingsAccount> branchAccounts = accountService.getAccountsByBranch(employee.getBranchId());
                            if (branchAccounts != null) {
                                userAccounts = branchAccounts;
                            }
                        }
                    }
                    
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    
                    if (userAccounts.isEmpty()) {
                        if ("XMLHttpRequest".equals(ajaxHeader)) {
                            sendAccountsJsonResponse(response, false, "No accounts found for user", null);
                        } else {
                            request.setAttribute("errorMessage", "No accounts found for user");
                            request.setAttribute("storageType", storageType != null ? storageType : "database");
                            request.getRequestDispatcher("account.jsp").forward(request, response);
                        }
                    } else {
                        if ("XMLHttpRequest".equals(ajaxHeader)) {
                            sendAccountsJsonResponse(response, true, "User accounts retrieved successfully", userAccounts);
                        } else {
                            request.setAttribute("userAccounts", userAccounts);
                            request.setAttribute("storageType", storageType != null ? storageType : "database");
                            request.getRequestDispatcher("account.jsp").forward(request, response);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String ajaxHeader = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendAccountsJsonResponse(response, false, "Error retrieving user accounts: " + e.getMessage(), null);
                    } else {
                        request.setAttribute("errorMessage", "Error retrieving user accounts: " + e.getMessage());
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            }
            

            else if ("getByCustomer".equals(action) && customerId != null) {
                int custId = Integer.parseInt(customerId);
                List<SavingsAccount> accounts = accountService.getAccountsByCustomer(custId);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (accounts == null || accounts.isEmpty()) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "No accounts found for customer ID: " + custId, null);
                    } else {
                        request.setAttribute("errorMessage", "No accounts found for customer ID: " + custId);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Accounts retrieved successfully for customer " + custId, accounts);
                    } else {
                        request.setAttribute("accounts", accounts);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showAccountsList", true);
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            }
            else {
                response.sendRedirect("account.jsp");
            }
        } catch (NumberFormatException e) {
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                sendJsonResponse(response, false, "Invalid input format", null);
            } else {
                request.setAttribute("errorMessage", "Invalid input format");
                request.getRequestDispatcher("account.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Error: " + e.getMessage();
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                sendJsonResponse(response, false, errorMsg, null);
            } else {
                request.setAttribute("errorMessage", errorMsg);
                request.getRequestDispatcher("account.jsp").forward(request, response);
            }
        }
    }
    
    /**
     * Handles POST requests for account modification operations.
     * Performs Dependency Injection by passing the user's choice of data storage into the Services.
     * Account creation, deposits, withdrawals, and joint account management.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String storageType = request.getParameter("storageType");
        
        try {
            DataStorage dataStorage = getDataStorage(storageType);
            AccountService accountService = new AccountService(dataStorage);
            
            if ("create".equals(action)) {
                try {
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                  
                    String balanceStr = request.getParameter("balance");
                    BigDecimal balance = new BigDecimal(balanceStr);
                    
                    int branchId = Integer.parseInt(request.getParameter("branchId"));
                    
                    BigDecimal minBalance = new BigDecimal("100.00");
                    if (balance.compareTo(minBalance) < 0) {
                        request.setAttribute("errorMessage", "Minimum balance of 100 is required");
                    } else {
                        int accountNo = accountService.createSavingsAccount(customerId, balance, branchId);
                        
                        if (accountNo > 0) {
                            request.setAttribute("successMessage", "User account created successfully");
                            request.setAttribute("newAccountNo", accountNo);
                            request.setAttribute("showCreateResult", true);
                        } else {
                            request.setAttribute("errorMessage", "Failed to create account. Customer ID may not exist or other database error occurred.");
                        }
                    }
                    request.setAttribute("storageType", storageType != null ? storageType : "database");
                    
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid input format. Please check all numeric fields.");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Error creating account: " + e.getMessage());
                }
            }
            
            else if ("withdraw".equals(action)) {
                try {
                    int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                    String amountStr = request.getParameter("amount");
                    BigDecimal amount = new BigDecimal(amountStr);
                    
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        request.setAttribute("errorMessage", "Withdrawal amount must be positive");
                    } else {
                        SavingsAccount account = accountService.getAccount(accountNo);
                        if (account == null) {
                            request.setAttribute("errorMessage", "Account not found with account number: " + accountNo);
                        } else {
                            // Get user context for logging
                            Integer userId = getCurrentUserId(request);
                            TransactionLog.UserType userType = getCurrentUserType(request);
                            String description = "Withdrawal via web interface";
                            
                            boolean success = accountService.performWithdrawal(accountNo, amount, userId, userType, description);
                            if (success) {
                                request.setAttribute("successMessage", "Withdrawal was successful");
                            } else {
                                request.setAttribute("errorMessage", "Withdrawal Failed - Insufficient balance or account error");
                            }
                        }
                    }
                    request.setAttribute("storageType", storageType != null ? storageType : "database");
                    request.setAttribute("showWithdrawResult", true);
                    
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid input format. Please check all numeric fields.");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Error during withdrawal: " + e.getMessage());
                }
            }


            else if ("deposit".equals(action)) {
                try {
                    int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                    String amountStr = request.getParameter("amount");
                    BigDecimal amount = new BigDecimal(amountStr);
                    
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        request.setAttribute("errorMessage", "Deposit amount must be positive");
                    } else {
                        SavingsAccount account = accountService.getAccount(accountNo);
                        if (account == null) {
                            request.setAttribute("errorMessage", "Account not found with account number: " + accountNo);
                        } else {
                            // Get user context for logging
                            Integer userId = getCurrentUserId(request);
                            TransactionLog.UserType userType = getCurrentUserType(request);
                            String description = "Deposit via web interface";
                            
                            boolean success = accountService.performDeposit(accountNo, amount, userId, userType, description);
                            if (success) {
                                request.setAttribute("successMessage", "Deposit Successful");
                            } else {
                                request.setAttribute("errorMessage", "Deposit Failed - Account error occurred");
                            }
                        }
                    }
                    request.setAttribute("storageType", storageType != null ? storageType : "database");
                    request.setAttribute("showDepositResult", true);
                    
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid input format. Please check all numeric fields.");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Error during deposit: " + e.getMessage());
                }
            }
            
            else if ("createJoint".equals(action)) {
                try {
                    String customerIdsParam = request.getParameter("customerIds");
                    if (customerIdsParam == null || customerIdsParam.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer IDs are required");
                    } else {
                        String[] customerIdsArray = customerIdsParam.split(",");
                        List<Integer> customerIds = new ArrayList<>();
                        for (String idStr : customerIdsArray) {
                            try {
                                int id = Integer.parseInt(idStr.trim());
                                customerIds.add(id);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid customer ID format: " + idStr);
                            }
                        }
                        
                        if (customerIds.size() < 2) {
                            request.setAttribute("errorMessage", "At least two valid customer IDs are required");
                        } else {
                            String balanceStr = request.getParameter("balance");
                            BigDecimal balance = new BigDecimal(balanceStr);  // Direct string to BigDecimal
                            int branchId = Integer.parseInt(request.getParameter("branchId"));
                            int accountNo = accountService.createJointSavingsAccount(customerIds, balance, branchId);
                            request.setAttribute("successMessage", "Joint account created successfully");
                            request.setAttribute("newAccountNo", accountNo);
                            request.setAttribute("showCreateResult", true);
                        }
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid input format. Check numeric fields.");
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Error: " + e.getMessage());
                }
            }


            else if ("addJointHolder".equals(action)) {
                try {
                    int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                    
                    boolean success = accountService.addCustomerToExistingAccount(customerId, accountNo);
                    
                    if (success) {
                        request.setAttribute("successMessage", "Customer " + customerId 
                            + " added to account " + accountNo);
                    } else {
                        request.setAttribute("errorMessage", "Failed to add joint holder");
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid account number or customer ID format");
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Error adding joint holder: " + e.getMessage());
                }
            }
            else if ("removeJointHolder".equals(action)) {
                try {
                    int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                    
                    boolean success = accountService.removeCustomerFromAccount(customerId, accountNo);
                    
                    if (success) {
                        request.setAttribute("successMessage", "Customer " + customerId 
                            + " removed from account " + accountNo);
                    } else {
                        request.setAttribute("errorMessage", "Failed to remove joint holder");
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid account number or customer ID format");
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Error removing joint holder: " + e.getMessage());
                }
            }
          

            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database connection error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Unexpected error: " + e.getMessage());
        }
        
        String ajaxHeader = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajaxHeader)) {
            String successMsg = (String) request.getAttribute("successMessage");
            String errorMsg = (String) request.getAttribute("errorMessage");
            
            if (successMsg != null) {
                Object newAccountNo = request.getAttribute("newAccountNo");
                sendJsonResponse(response, true, successMsg, newAccountNo);
            } else if (errorMsg != null) {
                sendJsonResponse(response, false, errorMsg, null);
            } else {
                sendJsonResponse(response, false, "Unknown error occurred", null);
            }
        } else {
            request.getRequestDispatcher("account.jsp").forward(request, response);
        }
    }
}