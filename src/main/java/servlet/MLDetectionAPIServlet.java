package servlet;

import filter.MLThreatDetectionFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * API Servlet to expose ML threat detection logs and statistics
 * Provides REST endpoints for the ML dashboard
 * 
 * @author TAMIL MUGHILAN
 */
@WebServlet(name = "MLDetectionAPI", urlPatterns = {"/api/ml-logs", "/api/ml-stats"})
public class MLDetectionAPIServlet extends HttpServlet {
    
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("✓ ML Detection API Servlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set CORS headers for development
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getServletPath();
        PrintWriter out = response.getWriter();
        
        try {
            if ("/api/ml-logs".equals(pathInfo)) {
                // Return ML detection logs
                List<MLThreatDetectionFilter.ThreatDetectionLog> logs = 
                    MLThreatDetectionFilter.getLogs();
                
                String json = gson.toJson(logs);
                out.print(json);
                
            } else if ("/api/ml-stats".equals(pathInfo)) {
                // Return detection statistics
                MLThreatDetectionFilter.DetectionStats stats = 
                    MLThreatDetectionFilter.getStats();
                
                String json = gson.toJson(stats);
                out.print(json);
                
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Endpoint not found\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
        
        out.flush();
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle CORS preflight
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}