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
            // Default to database if not specified or invalid
            return new DatabaseStorage();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String customerId = request.getParameter("customerId");
        String storageType = request.getParameter("storageType");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if ("get".equals(action) && customerId != null) {
            try {
                int id = Integer.parseInt(customerId);
                
                // Create service with user-selected storage
                DataStorage dataStorage = getDataStorage(storageType);
                CustomerService customerService = new CustomerService(dataStorage);
                
                Customer customer = customerService.getCustomer(id);

                out.println("<html><body>");
                if (customer != null) {
                    out.println("<h2>Customer Details</h2>");
                    out.println("<p><strong>Storage Type:</strong> " + (storageType != null ? storageType : "database") + "</p>");
                    out.println(customer.toString().replace("\n", "<br>"));
                } else {
                    out.println("<h2>Customer not found</h2>");
                }
                out.println("<br><a href='customer.html'>Back</a>");
                out.println("</body></html>");
            } catch (NumberFormatException e) {
                out.println("<html><body><h2>Invalid Customer ID</h2></body></html>");
            } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            response.sendRedirect("customer.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String storageType = request.getParameter("storageType");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            DataStorage dataStorage = getDataStorage(storageType);
            CustomerService customerService = new CustomerService(dataStorage);
            
            if ("update".equals(action)) {
                // Handle update
                int customerId = Integer.parseInt(request.getParameter("customerId"));
                String name = request.getParameter("name");
                String phone = request.getParameter("phone");
                String email = request.getParameter("email");
                int branchId = Integer.parseInt(request.getParameter("branchId"));
                
                customerService.updateCustomer(customerId, name, phone, email, branchId);
                out.println("<h2>Customer Updated Successfully</h2>");
                
            } else if ("delete".equals(action)) {
                // Handle delete
                int customerId = Integer.parseInt(request.getParameter("customerId"));
                customerService.deleteCustomer(customerId);
                out.println("<h2>Customer Deleted Successfully</h2>");
                
            } else {
                throw new ServletException("Invalid action");
            }
            
            out.println("<a href='customer.html'>Back</a>");
            
        } catch (Exception e) {
            out.println("<h2>Error: " + e.getMessage() + "</h2>");
            out.println("<a href='customer.html'>Back</a>");
        }
    }

}