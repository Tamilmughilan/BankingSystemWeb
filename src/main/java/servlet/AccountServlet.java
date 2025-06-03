package servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import service.AccountService;
import storage.DataStorage;
import storage.DatabaseStorage;
import storage.CollectionStorage;
import entity.SavingsAccount;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    
    private DataStorage getDataStorage(String storageType) throws SQLException {
        if ("database".equalsIgnoreCase(storageType)) {
            return new DatabaseStorage();
        } else if ("collection".equalsIgnoreCase(storageType)) {
            return new CollectionStorage();
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
            json.append(",\"data\":\"").append(data.toString().replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
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
        String storageType = request.getParameter("storageType");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        if ("get".equals(action) && accountNo != null) {
            try {
                int accNo = Integer.parseInt(accountNo);
                DataStorage dataStorage = getDataStorage(storageType);
                AccountService accountService = new AccountService(dataStorage);
                SavingsAccount account = accountService.getAccount(accNo);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (account == null) {
                    // Account not found
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Account not found with account number: " + accNo, null);
                    } else {
                        request.setAttribute("errorMessage", "Account not found with account number: " + accNo);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                } else {
                    // Account found
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Account retrieved successfully", account);
                    } else {
                        request.setAttribute("account", account);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showAccountDetails", true);
                        request.getRequestDispatcher("account.jsp").forward(request, response);
                    }
                }
            } catch (NumberFormatException e) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    sendJsonResponse(response, false, "Invalid Account Number format", null);
                } else {
                    request.setAttribute("errorMessage", "Invalid Account Number format");
                    request.getRequestDispatcher("account.jsp").forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = "Database error: " + e.getMessage();
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    sendJsonResponse(response, false, errorMsg, null);
                } else {
                    request.setAttribute("errorMessage", errorMsg);
                    request.getRequestDispatcher("account.jsp").forward(request, response);
                }
            }
        } else {
            response.sendRedirect("account.jsp");
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
                    double balance = Double.parseDouble(request.getParameter("balance"));
                    int branchId = Integer.parseInt(request.getParameter("branchId"));
                    
                    // Validate minimum balance
                    if (balance < 100) {
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
                    double amount = Double.parseDouble(request.getParameter("amount"));
                    
                    if (amount <= 0) {
                        request.setAttribute("errorMessage", "Withdrawal amount must be positive");
                    } else {
                        // First check if account exists
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
                    double amount = Double.parseDouble(request.getParameter("amount"));
                    
                    if (amount <= 0) {
                        request.setAttribute("errorMessage", "Deposit amount must be positive");
                    } else {
                        // First check if account exists
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
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database connection error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Unexpected error: " + e.getMessage());
        }
        
        // Handle AJAX vs regular form submission
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