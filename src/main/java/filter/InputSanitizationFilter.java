package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.net.URLDecoder;

/**
 * Filter that validates and sanitizes user input to prevent XSS and injection attacks.
 * Blocks requests containing dangerous patterns or invalid data.
 *
 * @author TAMIL MUGHILAN
 */
public class InputSanitizationFilter implements Filter {
    
    
    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile("(?i)<script[^>]*>.*?</script>"),
        Pattern.compile("(?i)<iframe[^>]*>.*?</iframe>"),
        Pattern.compile("(?i)<object[^>]*>.*?</object>"),
        Pattern.compile("(?i)<embed[^>]*>.*?</embed>"),
        Pattern.compile("(?i)<link[^>]*>"),
        Pattern.compile("(?i)<meta[^>]*>"),
        Pattern.compile("(?i)javascript:"),
        Pattern.compile("(?i)vbscript:"),
        Pattern.compile("(?i)onload\\s*="),
        Pattern.compile("(?i)onerror\\s*="),
        Pattern.compile("(?i)onclick\\s*="),
        Pattern.compile("(?i)onmouseover\\s*="),
        Pattern.compile("(?i)eval\\s*\\("),
        Pattern.compile("(?i)expression\\s*\\("),
        Pattern.compile("(?i)document\\.cookie"),
        Pattern.compile("(?i)document\\.write"),
        Pattern.compile("(?i)<.*?\\s+on\\w+\\s*=.*?>")
    };
    /**
     * Initializes the filter when application starts.
     *
     * @param filterConfig the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Enhanced Input Sanitization Filter Initialized");
    }
    
    /**
     * Cleans up resources when filter is destroyed.
     */
    @Override
    public void destroy() {}
    
    
    /**
     * Processes requests to validate all input parameters.
     * Blocks requests containing dangerous or invalid input.
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
        
        // Skip validation for static resources
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        Map<String, String[]> params = req.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String paramName = entry.getKey();
            for (String value : entry.getValue()) {
                if (!isSafe(paramName, value)) {
                    System.out.println("SECURITY ALERT: Dangerous input detected in parameter: " + paramName);
                    System.out.println("Value: " + (value.length() > 100 ? value.substring(0, 100) + "..." : value));
                    
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.setContentType("text/html");
                    res.getWriter().println(
                        "<html><body>" +
                        "<h2>Security Error</h2>" +
                        "<p>Invalid input detected. Please check your input and try again.</p>" +
                        "<a href='javascript:history.back()'>Go Back</a>" +
                        "</body></html>"
                    );
                    return;
                }
            }
        }
        
        System.out.println("Input validation passed for: " + path);
        chain.doFilter(request, response);
    }
    
    /**
     * Checks if the requested path is a static resource.
     *
     * @param path the request path to check
     * @return true if it's a static resource, false otherwise
     */
    private boolean isStaticResource(String path) {
        return path.endsWith(".css") || path.endsWith(".js") || 
               path.endsWith(".png") || path.endsWith(".jpg") || 
               path.endsWith(".jpeg") || path.endsWith(".gif") ||
               path.endsWith(".ico");
    }
    
    /**
     * Validates if a parameter value is safe to process.
     *
     * @param paramName the parameter name
     * @param value the parameter value
     * @return true if safe, false if dangerous
     */
    private boolean isSafe(String paramName, String value) {
        if (value == null) return true;
        
        
        try {
            String decodedValue = URLDecoder.decode(value, "UTF-8");
            
            if (!validateContent(value) || !validateContent(decodedValue)) {
                return false;
            }
        } catch (Exception e) {
            
            if (!validateContent(value)) {
                return false;
            }
        }
        
        value = value.trim();
        if (value.isEmpty()) return true;
        
        
        switch (paramName.toLowerCase()) {
            case "email":
                return validateEmail(value);
            case "phone":
                return validatePhone(value);
            case "amount":
                return validateAmount(value);
            case "name":
                return validateName(value);
            case "csrftoken":
                return validateCSRFToken(value);
            case "password":
                return validatePassword(value);
            default:
                if (paramName.toLowerCase().endsWith("id")) {
                    return validateId(value);
                }
                return validateGeneral(value);
        }
    }
    
    private boolean validateContent(String value) {
        if (value == null) return true;
        
        
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return false;
            }
        }
        
        
        if (value.matches(".*<[^>]+>.*<[^>]+>.*")) {
            return false;
        }
        
        return true;
    }
    
    
    private boolean validateEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") && 
               email.length() <= 255;
    }
    
    private boolean validatePhone(String phone) {
        return phone.matches("^\\d{10}$");
    }
    
    private boolean validateAmount(String amount) {
        return amount.matches("^\\d+(\\.\\d{1,2})?$") && 
               amount.length() <= 15;
    }
    
    private boolean validateName(String name) {
        return name.matches("^[a-zA-Z .'-]{1,100}$");
    }
    
    private boolean validateId(String id) {
        return id.matches("^\\d+$") && 
               id.length() <= 19; 
    }
    
    private boolean validateCSRFToken(String token) {

        return token.matches("^[A-Za-z0-9+/=]{40,}$") && 
               token.length() <= 256;
    }
    
    private boolean validatePassword(String password) {
        
        return !password.matches(".*[<>\"'&].*") && 
               password.length() >= 8 && 
               password.length() <= 128;
    }
    
    private boolean validateGeneral(String value) {
        
        return !value.matches(".*<[^>]*>.*") && 
               value.length() <= 1000 &&
               !containsDangerousSequences(value);
    }
    
    private boolean containsDangerousSequences(String value) {
        String lower = value.toLowerCase();
        return lower.contains("javascript:") || 
               lower.contains("vbscript:") || 
               lower.contains("data:text/html") ||
               lower.contains("&#") || 
               lower.contains("%3c") || 
               lower.contains("%3e") || 
               lower.contains("\\u003c") ||
               lower.contains("\\u003e"); 
    }
}