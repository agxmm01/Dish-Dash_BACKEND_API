package in.agampal.dishdashapi.config;

import in.agampal.dishdashapi.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Rate limiting configuration
    private static final int MAX_REQUESTS = 100; // requests per minute
    private static final long TIME_WINDOW = 60 * 1000; // 1 minute in milliseconds

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String key = clientIp + ":" + request.getRequestURI();
        
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());
        
        long currentTime = System.currentTimeMillis();
        
        // Reset counter if time window has passed
        if (currentTime - rateLimitInfo.getFirstRequestTime() > TIME_WINDOW) {
            rateLimitInfo.reset(currentTime);
        }
        
        // Check if rate limit exceeded
        if (rateLimitInfo.getRequestCount().get() >= MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, request.getRequestURI());
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ApiResponse<Object> errorResponse = ApiResponse.error(
                "Rate limit exceeded. Please try again later.",
                "RATE_LIMIT_EXCEEDED"
            );
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }
        
        // Increment request count
        rateLimitInfo.getRequestCount().incrementAndGet();
        
        return true;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private static class RateLimitInfo {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private long firstRequestTime = System.currentTimeMillis();
        
        public AtomicInteger getRequestCount() {
            return requestCount;
        }
        
        public long getFirstRequestTime() {
            return firstRequestTime;
        }
        
        public void reset(long currentTime) {
            requestCount.set(0);
            firstRequestTime = currentTime;
        }
    }
}


