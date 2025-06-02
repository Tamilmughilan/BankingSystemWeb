package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>Banking System</title></head><body>");
        out.println("<h1>Banking System Home</h1>");
        out.println("<p><a href='customer.html'>Customer Management</a></p>");
        out.println("<p><a href='account.html'>Account Management</a></p>");
        out.println("</body></html>");
    }
}