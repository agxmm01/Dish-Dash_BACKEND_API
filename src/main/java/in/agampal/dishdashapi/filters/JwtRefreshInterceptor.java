package in.agampal.dishdashapi.filters;

//package in.agampal.dishdashapi.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.agampal.dishdashapi.dto.ApiResponse;
import in.agampal.dishdashapi.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRefreshInterceptor extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Skip auth endpoints
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Let JwtAuthenticationFilter handle this
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            jwtUtil.extractUsername(token);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("Access token expired for request: {}", requestURI);

            String refreshToken = request.getHeader("X-Refresh-Token");

            if (refreshToken == null || refreshToken.isEmpty()) {
                sendError(response, "Access token expired. Please use refresh token to get new access token.", "TOKEN_EXPIRED");
                return;
            }

            try {
                String username = jwtUtil.extractUsername(refreshToken);
                if (jwtUtil.isRefreshToken(refreshToken) && !jwtUtil.isTokenExpired(refreshToken)) {
                    // Generate new access token
                    String newAccessToken = jwtUtil.generateTokenFromUsername(username);

                    response.setHeader("X-New-Access-Token", newAccessToken);
                    response.setHeader("X-Token-Refreshed", "true");

                    log.info("Token automatically refreshed for user: {}", username);
                    filterChain.doFilter(request, response);
                } else {
                    sendError(response, "Refresh token expired or invalid. Please login again.", "REFRESH_TOKEN_EXPIRED");
                }
            } catch (Exception refreshException) {
                log.error("Error validating refresh token", refreshException);
                sendError(response, "Invalid refresh token. Please login again.", "INVALID_REFRESH_TOKEN");
            }
        } catch (Exception e) {
            log.error("Error processing token in JwtRefreshInterceptor", e);
            filterChain.doFilter(request, response);
        }
    }

    private void sendError(HttpServletResponse response, String message, String code) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> errorResponse = ApiResponse.error(message, code);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}



