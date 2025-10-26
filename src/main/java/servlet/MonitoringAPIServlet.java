package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;
import filter.RequestLoggerFilter;

/**
 * API endpoints for monitoring dashboard
 */
@WebServlet({"/api/logs", "/api/stats"})
public class MonitoringAPIServlet extends HttpServlet {
    
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Enable CORS for local development
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String path = request.getServletPath();
        PrintWriter out = response.getWriter();
        
        if ("/api/logs".equals(path)) {
            // Return request logs
            out.print(gson.toJson(RequestLoggerFilter.getLogs()));
        } else if ("/api/stats".equals(path)) {
            // Return statistics
            out.print(gson.toJson(RequestLoggerFilter.getStats()));
        } else {
            response.setStatus(404);
            out.print("{\"error\": \"Not found\"}");
        }
        
        out.flush();
    }
}