package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long TIME_WINDOW_MS = 60 * 1000;
    private final Map<Integer, List<Long>> requestMap = new HashMap<>();
    
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("\nRate Limiting Filter Initialized.\n");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        if (session != null && "CUSTOMER".equals(session.getAttribute("role"))) {
            int userId = (int) session.getAttribute("userId");
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
        
        System.out.println("\nRate limiter filter moving to Logging Filter");
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}