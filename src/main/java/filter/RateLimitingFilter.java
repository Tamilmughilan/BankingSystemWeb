package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * Filter that restricts user from making DoS attacks and abuse
 * Limits number of requests using a sliding window
 * 
 * @author TAMIL MUGHILAN
 */
public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final long TIME_WINDOW_MS = 60 * 1000;
    private final Map<Integer, List<Long>> requestMap = new HashMap<>();
    
    /**
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("\nRate Limiting Filter Initialized.\n");
    }
    
    /**
     * Processes requests to enforce rate limiting on customers.
     * Blocks requests that exceed the allowed rate limit.
     *
     * @param request the servlet request
     * @param response the servlet response  
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String uri = req.getRequestURI();
        
        // Skip rate limiting for static resources
        if (isStaticResource(path) || isStaticResource(uri)) {
            System.out.println("RateLimiting: Allowing static resource: " + uri);
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = req.getSession(false);
        
        if (session != null && "CUSTOMER".equals(session.getAttribute("role"))) {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId != null) {
                long currentTime = System.currentTimeMillis();
                
                List<Long> timestamps = requestMap.getOrDefault(userId, new ArrayList<>());
                
                // Remove old timestamps outside the time window
                Iterator<Long> iterator = timestamps.iterator();
                while (iterator.hasNext()) {
                    Long t = iterator.next();
                    if (currentTime - t > TIME_WINDOW_MS) {
                        iterator.remove();
                    }
                }
                
                if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                    res.setStatus(429);
                    res.setContentType("text/html");
                    res.getWriter().println("Too many requests. Rate limit exceeded, try again after some time.");
                    return;
                } else {
                    timestamps.add(currentTime);
                    requestMap.put(userId, timestamps);
                }
            }
        }
        
        System.out.println("\nRate limiter filter moving to next filter");
        chain.doFilter(request, response);
    }
    
    /**
     * Checks if the requested path is a static resource.
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
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {}
}