package servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import java.util.List;

import service.CustomerService;
import storage.DataStorage;
import storage.DatabaseStorage;
import storage.MongoDBStorage;
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
	    } else if ("mongodb".equalsIgnoreCase(storageType)) {
	        return new MongoDBStorage();
	    } else {
	        return new DatabaseStorage(); // default
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
        String branchId = request.getParameter("branchId");
        String email = request.getParameter("email");
        String storageType = request.getParameter("storageType");

        try {
            DataStorage dataStorage = getDataStorage(storageType);
            CustomerService customerService = new CustomerService(dataStorage);
            
            if ("get".equals(action) && customerId != null) {
                int id = Integer.parseInt(customerId);
                Customer customer = customerService.getCustomer(id);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (customer == null) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Customer not found with ID: " + id, null);
                    } else {
                        request.setAttribute("errorMessage", "Customer not found with ID: " + id);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Customer retrieved successfully", customer);
                    } else {
                        request.setAttribute("customer", customer);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showCustomerDetails", true);
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                }
            } 
            else if ("getByEmail".equals(action) && email != null) {
                Customer customer = customerService.getCustomerByEmail(email);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (customer == null) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "Customer not found with email: " + email, null);
                    } else {
                        request.setAttribute("errorMessage", "Customer not found with email: " + email);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Customer retrieved successfully", customer);
                    } else {
                        request.setAttribute("customer", customer);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showCustomerDetails", true);
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                }
            }
            else if ("getByBranch".equals(action) && branchId != null) {
                int brId = Integer.parseInt(branchId);
                List<Customer> customers = customerService.getCustomersByBranch(brId);
                
                String ajaxHeader = request.getHeader("X-Requested-With");
                
                if (customers == null || customers.isEmpty()) {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, false, "No customers found for branch ID: " + brId, null);
                    } else {
                        request.setAttribute("errorMessage", "No customers found for branch ID: " + brId);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                } else {
                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        sendJsonResponse(response, true, "Customers retrieved successfully for branch " + brId, customers);
                    } else {
                        request.setAttribute("customers", customers);
                        request.setAttribute("storageType", storageType != null ? storageType : "database");
                        request.setAttribute("showCustomersList", true);
                        request.getRequestDispatcher("customer.jsp").forward(request, response);
                    }
                }
            }
            else {
                response.sendRedirect("customer.jsp");
            }
        } catch (NumberFormatException e) {
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                sendJsonResponse(response, false, "Invalid input format", null);
            } else {
                request.setAttribute("errorMessage", "Invalid input format");
                request.getRequestDispatcher("customer.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Error: " + e.getMessage();
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                sendJsonResponse(response, false, errorMsg, null);
            } else {
                request.setAttribute("errorMessage", errorMsg);
                request.getRequestDispatcher("customer.jsp").forward(request, response);
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
            CustomerService customerService = new CustomerService(dataStorage);
            
            if ("update".equals(action)) {
                try {
                    int customerId = Integer.parseInt(request.getParameter("customerId"));
                    String name = request.getParameter("name");
                    String phone = request.getParameter("phone");
                    String email = request.getParameter("email");
                    int branchId = Integer.parseInt(request.getParameter("branchId"));
                    
                    if (name == null || name.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer name is required");
                    } else if (email == null || email.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer email is required");
                    } else if (phone == null || phone.trim().isEmpty()) {
                        request.setAttribute("errorMessage", "Customer phone is required");
                    } else {
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