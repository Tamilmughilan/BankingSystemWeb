package servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import service.CustomerService;
import storage.DataStorage;
import storage.DatabaseStorage;
import storage.CollectionStorage;
import entity.Customer;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/customer")
public class CustomerServlet extends HttpServlet {
    
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
        String customerId = request.getParameter("customerId");
        String storageType = request.getParameter("storageType");

        if ("get".equals(action) && customerId != null) {
            try {
                int id = Integer.parseInt(customerId);
                
                DataStorage dataStorage = getDataStorage(storageType);
                CustomerService customerService = new CustomerService(dataStorage);
                
                Customer customer = customerService.getCustomer(id);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (customer == null) {
                    // Customer not found
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Customer not found with ID: " + id, null);
                    } else {
                        request.setAttribute("errorMessage", "Customer not found with ID: " + id);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                } else {
                    // Customer found
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Customer retrieved successfully", customer);
                    } else {
                        request.setAttribute("customer", customer);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showCustomerDetails", true);
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                }
            } catch (NumberFormatException e) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    sendJsonResponse(response, false, "Invalid Customer ID format", null);
                } else {
                    request.setAttribute("errorMessage", "Invalid Customer ID format");
                    request.getRequestDispatcher("customer.jsp").forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = "Database error: " + e.getMessage();
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    sendJsonResponse(response, false, errorMsg, null);
                } else {
                    request.setAttribute("errorMessage", errorMsg);
                    request.getRequestDispatcher("customer.jsp").forward(request, response);
                }
            }
        } else {
            response.sendRedirect("customer.jsp");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String storageType = request.getParameter("storageType");

        try {
            DataStorage dataStorage = getDataStorage(storageType);
            CustomerService customerService = new CustomerService(dataStorage);
            
            if ("update".equals(action)) {
                try {
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                    String name = request.getParameter("name");
                    String phone = request.getParameter("phone");
                    String email = request.getParameter("email");
                    int branchId = Integer.parseInt(request.getParameter("branchId"));
                    
                    // Validate inputs
                    if (name == null || name.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer name is required");
                    } else if (email == null || email.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer email is required");
                    } else if (phone == null || phone.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer phone is required");
                    } else {
                        // First check if customer exists
                        Customer existingCustomer = customerService.getCustomer(customerId);
                        if (existingCustomer == null) {
                            request.setAttribute("errorMessage", "Customer not found with ID: " + customerId);
                        } else {
                            customerService.updateCustomer(customerId, name.trim(), phone.trim(), email.trim(), branchId);
                            request.setAttribute("successMessage", "Customer Updated Successfully");
                            request.setAttribute("showUpdateResult", true);
                        }
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid input format. Please check all numeric fields.");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Error updating customer: " + e.getMessage());
                }
                
            } else if ("delete".equals(action)) {
                try {
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                    
                    // First check if customer exists
                    Customer existingCustomer = customerService.getCustomer(customerId);
                    if (existingCustomer == null) {
                        request.setAttribute("errorMessage", "Customer not found with ID: " + customerId);
                    } else {
                        boolean deleted = customerService.deleteCustomer(customerId);
                        if (deleted) {
                            request.setAttribute("successMessage", "Customer Deleted Successfully");
                            request.setAttribute("showDeleteResult", true);
                        } else {
                            request.setAttribute("errorMessage", "Failed to delete customer. Customer may have associated accounts.");
                        }
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Invalid Customer ID format");
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
                }
                
            } else {
                request.setAttribute("errorMessage", "Invalid action specified");
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
                sendJsonResponse(response, true, successMsg, null);
            } else if (errorMsg != null) {
                sendJsonResponse(response, false, errorMsg, null);
            } else {
                sendJsonResponse(response, false, "Unknown error occurred", null);
            }
        } else {
            request.getRequestDispatcher("customer.jsp").forward(request, response);
        }
    }
}