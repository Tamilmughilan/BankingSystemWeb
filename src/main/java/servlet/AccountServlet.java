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
import entity.SavingsAccount;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    
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
                    BigDecimal amount = new BigDecimal(amountStr);  // Direct string to BigDecimal
                    
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        request.setAttribute("errorMessage", "Withdrawal amount must be positive");
                    } else {
                        SavingsAccount account = accountService.getAccount(accountNo);
                        if (account == null) {
                            request.setAttribute("errorMessage", "Account not found with account number: " + accountNo);
                        } else {
                            boolean success = accountService.performWithdrawal(accountNo, amount);
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
                    BigDecimal amount = new BigDecimal(amountStr);  // Direct string to BigDecimal
                    
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        request.setAttribute("errorMessage", "Deposit amount must be positive");
                    } else {
                        SavingsAccount account = accountService.getAccount(accountNo);
                        if (account == null) {
                            request.setAttribute("errorMessage", "Account not found with account number: " + accountNo);
                        } else {
                            boolean success = accountService.performDeposit(accountNo, amount);
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