package filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Filter to log all requests to banking application
 * Records request details, response times, and detects suspicious patterns
 * 
 * @author TAMIL MUGHILAN
 */
public class RequestLoggerFilter implements Filter {
    
    // Store logs in memory (simple for demo)
    private static final List<RequestLog> REQUEST_LOGS = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOGS = 1000;
    
    /**
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("=".repeat(80));
        System.out.println("🔍 REQUEST LOGGER STARTED - Monitoring all requests");
        System.out.println("=".repeat(80));
    }
    
    /**
     * Filters requests and logs details including timing and security analysis.
     * Skips logging for static files.
     *
     * @param request the servlet request
     * @param response the servlet response  
     * @param chain the filter chain to continue processing
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getServletPath();
        String uri = httpRequest.getRequestURI();
        
        // Skip logging for static resources
        if (isStaticResource(path) || isStaticResource(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        // Extract request details
        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();
        String fullPath = uri + (queryString != null ? "?" + queryString : "");
        String clientIp = httpRequest.getRemoteAddr();
        
        // Use try-finally to ensure logging happens even if request is blocked
        int statusCode = 200;
        try {
            // Continue the request
            chain.doFilter(request, response);
            statusCode = httpResponse.getStatus();
        } catch (Exception e) {
            statusCode = 500;
            throw e;
        } finally {
            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Detect suspicious patterns
            boolean isSuspicious = detectSuspiciousPattern(fullPath, queryString);
            
            // Create log entry
            RequestLog log = new RequestLog();
            log.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            log.method = method;
            log.path = fullPath;
            log.clientIp = clientIp;
            log.statusCode = statusCode;
            log.processingTime = processingTime;
            log.isSuspicious = isSuspicious;
            log.suspiciousReason = isSuspicious ? detectReason(fullPath, queryString) : "Normal";
            
            // Add to logs
            synchronized (REQUEST_LOGS) {
                REQUEST_LOGS.add(log);
                if (REQUEST_LOGS.size() > MAX_LOGS) {
                    REQUEST_LOGS.remove(0);
                }
            }
            
            // Print to console
            String status = isSuspicious ? "🚨 SUSPICIOUS" : "✓ Normal";
            System.out.println(String.format("[%s] %s | %s %s | Status: %d | Time: %dms",
                log.timestamp.substring(11, 19), status, method, uri, statusCode, processingTime));
        }
    }
    
    /**
     * Checks if the requested path is a static resource like CSS, JS, or images.
     *
     * @param path the request path to check
     * @return true if it's a static resource, false otherwise
     */
    private boolean isStaticResource(String path) {
        if (path == null) return false;
        String lowercasePath = path.toLowerCase();
        return lowercasePath.endsWith(".css") || 
               lowercasePath.endsWith(".js") || 
               lowercasePath.endsWith(".png") || 
               lowercasePath.endsWith(".jpg") || 
               lowercasePath.endsWith(".jpeg") || 
               lowercasePath.endsWith(".gif") ||
               lowercasePath.endsWith(".ico") ||
               lowercasePath.endsWith(".svg") ||
               lowercasePath.endsWith(".woff") ||
               lowercasePath.endsWith(".woff2") ||
               lowercasePath.endsWith(".ttf") ||
               lowercasePath.endsWith(".eot");
    }
    
    /**
     * Detect suspicious patterns in request
     * 
     * @param path the request path
     * @param queryString the query string parameters
     * @return true if suspicious pattern detected
     */
    private boolean detectSuspiciousPattern(String path, String queryString) {
        if (queryString == null) return false;
        
        String query = queryString.toLowerCase();
        
        // Check for SQL injection patterns
        if (query.contains("'") || query.contains("\"") || 
            query.contains("or 1=1") || query.contains("or '1'='1") ||
            query.contains("union") || query.contains("select") ||
            query.contains("drop") || query.contains("delete") ||
            query.contains("insert") || query.contains("update")) {
            return true;
        }
        
        // Check for XSS patterns
        if (query.contains("<script") || query.contains("javascript:") ||
            query.contains("onerror") || query.contains("onload")) {
            return true;
        }
        
        // Check for path traversal
        if (query.contains("../") || query.contains("..\\")) {
            return true;
        }
        
        // Check for very long parameters (possible buffer overflow)
        if (queryString.length() > 500) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Detect reason for suspicious activity
     * 
     * @param path the request path
     * @param queryString the query string parameters
     * @return description of suspicious pattern
     */
    private String detectReason(String path, String queryString) {
        if (queryString == null) return "Unknown";
        
        String query = queryString.toLowerCase();
        
        if (query.contains("'") || query.contains("or 1=1") || 
            query.contains("union") || query.contains("select")) {
            return "Possible SQL Injection";
        }
        if (query.contains("<script") || query.contains("javascript:")) {
            return "Possible XSS Attack";
        }
        if (query.contains("../")) {
            return "Path Traversal Attempt";
        }
        if (queryString.length() > 500) {
            return "Abnormally Long Request";
        }
        
        return "Suspicious Pattern Detected";
    }
    
    /**
     * Get all logs (for API endpoint)
     * 
     * @return list of all request logs
     */
    public static List<RequestLog> getLogs() {
        synchronized (REQUEST_LOGS) {
            return new ArrayList<>(REQUEST_LOGS);
        }
    }
    
    /**
     * Get statistics about requests
     * 
     * @return statistics object with aggregated data
     */
    public static Stats getStats() {
        synchronized (REQUEST_LOGS) {
            Stats stats = new Stats();
            stats.totalRequests = REQUEST_LOGS.size();
            stats.suspiciousRequests = (int) REQUEST_LOGS.stream().filter(l -> l.isSuspicious).count();
            stats.normalRequests = stats.totalRequests - stats.suspiciousRequests;
            
            if (stats.totalRequests > 0) {
                stats.avgProcessingTime = REQUEST_LOGS.stream()
                    .mapToLong(l -> l.processingTime)
                    .average()
                    .orElse(0);
            }
            
            return stats;
        }
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {
        REQUEST_LOGS.clear();
    }
    
    /**
     * Inner class representing a single request log entry
     */
    public static class RequestLog {
        public String timestamp;
        public String method;
        public String path;
        public String clientIp;
        public int statusCode;
        public long processingTime;
        public boolean isSuspicious;
        public String suspiciousReason;
    }
    
    /**
     * Inner class representing aggregated statistics
     */
    public static class Stats {
        public int totalRequests;
        public int suspiciousRequests;
        public int normalRequests;
        public double avgProcessingTime;
    }
}