package be.kuleuven.safetyrestservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor{

    private static final String API_KEY_HEADER = "Authorization";
    private static final String API_KEY = "Iw8zeveVyaPNWonPNaU0213uw3g6Ei"; // HARDCODED API KEY

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && apiKey.startsWith("Bearer ") && apiKey.substring(7).equals(API_KEY)) {
            return true; // Valid API key
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid or missing API key.");
            return false; // Block the request
        }
    }
}
