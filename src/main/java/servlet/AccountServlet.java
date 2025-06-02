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
            //Default
            return new DatabaseStorage();
        }
    }
    
    //GET - Account details for a particular Account number
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
                
                //Creates a service with user selected storage
                DataStorage dataStorage = getDataStorage(storageType);
                AccountService accountService = new AccountService(dataStorage);
                
                SavingsAccount account = accountService.getAccount(accNo);
                
                out.println("<html><body>");
                if (account != null) {
                    out.println("<h2>Account Details</h2>");
                    out.println("<p><strong>Storage Type:</strong> " + (storageType != null ? storageType : "database") + "</p>");
                    out.println(account.toString());
                } else {
                    out.println("<h2>Account not found</h2>");
                }
                out.println("<br><a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            } catch (NumberFormatException e) {
                out.println("<html><body><h2>Invalid Account Number</h2></body></html>");
            } catch (SQLException e) {
				
				e.printStackTrace();
			}
        } else {
            response.sendRedirect("account.jsp");
        }
    }
    
    //POST - Creating an Account, Withdraw from an account, Deposit to an account
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String storageType = request.getParameter("storageType");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
       
        DataStorage dataStorage = null;
		try {
			dataStorage = getDataStorage(storageType);
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
        AccountService accountService = new AccountService(dataStorage);
        
        if ("create".equals(action)) {
            try {
                int customerId = Integer.parseInt(request.getParameter("customerId"));
                double balance = Double.parseDouble(request.getParameter("balance"));
                int branchId = Integer.parseInt(request.getParameter("branchId"));
                
                int accountNo = accountService.createSavingsAccount(customerId, balance, branchId);
                
                out.println("<html><body>");
                out.println("<h2>Account Created Successfully</h2>");
                out.println("<p><strong>Storage Type:</strong> " + (storageType != null ? storageType : "database") + "</p>");
                out.println("<p>Account Number: " + accountNo + "</p>");
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            } catch (Exception e) {
                out.println("<html><body>");
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            }
        }
        //Withdraw
        else if ("withdraw".equals(action)) {
            try {
                int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                double amount = Double.parseDouble(request.getParameter("amount"));
                
                boolean success = accountService.performWithdrawal(accountNo, amount);
                
                out.println("<html><body>");
                out.println("<p><strong>Storage Type:</strong> " + (storageType != null ? storageType : "database") + "</p>");
                if (success) {
                    out.println("<h2>Withdrawal Successful</h2>");
                } else {
                    out.println("<h2>Withdrawal Failed</h2>");
                }
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            } catch (Exception e) {
                out.println("<html><body>");
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            }
        }
        //Deposit
        else if ("deposit".equals(action)) {
            try {
                int accountNo = Integer.parseInt(request.getParameter("accountNo"));
                double amount = Double.parseDouble(request.getParameter("amount"));
                
                boolean success = accountService.performDeposit(accountNo, amount);
                
                out.println("<html><body>");
                out.println("<p><strong>Storage Type:</strong> " + (storageType != null ? storageType : "database") + "</p>");
                if (success) {
                    out.println("<h2>Deposit Successful</h2>");
                } else {
                    out.println("<h2>Deposit Failed</h2>");
                }
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            } catch (Exception e) {
                out.println("<html><body>");
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
                out.println("<a href='account.jsp'>Back</a>");
                out.println("</body></html>");
            }
        }
    }
}