package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class InputSanitizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("Input Sanitization Filter Initialized");
    }

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        Map<String, String[]> params = req.getParameterMap();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String paramName = entry.getKey();
            for (String value : entry.getValue()) {
                if (!isSafe(paramName, value)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.getWriter().println("Invalid input detected in: " + paramName + " → " + value);
                    return;
                }
            }
        }
        
        System.out.println("Headers validation passed for: " + path);
        chain.doFilter(request, response);
    }

    private boolean isSafe(String paramName, String value) {
        if (value == null || value.trim().isEmpty()) return true;

        value = value.trim();

        //Email validation
        if (paramName.equalsIgnoreCase("email")) {
            return value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        }
        //Phone validation - 10 digits
        if (paramName.equalsIgnoreCase("phone")) {
            return value.matches("^\\d{10}$");
        }

        //Amount validation
        if (paramName.equalsIgnoreCase("amount")) {
            return value.matches("^\\d+(\\.\\d{1,2})?$");
        }

        // Name validation (allows alphabets, space, dot, apostrophe)
        if (paramName.equalsIgnoreCase("name")) {
            return value.matches("^[a-zA-Z .'-]{1,100}$");
        }

        //ID validation
        if (paramName.toLowerCase().endsWith("id")) {
            return value.matches("^\\d+$");
        }

        
        if (value.toLowerCase().contains("<script") || value.toLowerCase().contains("</script>")) {
            return false;
        }

        // General length limit
        return value.length() <= 100;
    }
}
