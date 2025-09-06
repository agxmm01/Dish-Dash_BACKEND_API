package in.agampal.dishdashapi.controller;

import in.agampal.dishdashapi.dto.ApiResponse;
import in.agampal.dishdashapi.io.AuthenticationRequest;
import in.agampal.dishdashapi.io.AuthenticationResponse;
import in.agampal.dishdashapi.io.TokenRefreshResponse;
import in.agampal.dishdashapi.service.AppUserDetailsService;
import in.agampal.dishdashapi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> login(
            @Valid @RequestBody AuthenticationRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            
            // Generate both access and refresh tokens
            final String accessToken = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            TokenRefreshResponse response = TokenRefreshResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(86400) // 24 hours in seconds
                    .build();
            
            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        try {
            // Remove "Bearer " prefix if present
            if (refreshToken.startsWith("Bearer ")) {
                refreshToken = refreshToken.substring(7);
            }
            
            log.info("Token refresh attempt");
            
            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken, userDetails)) {
                log.warn("Invalid refresh token for user: {}", username);
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
            }
            
            // Generate new tokens
            final String newAccessToken = jwtUtil.generateToken(userDetails);
            final String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            TokenRefreshResponse response = TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(86400) // 24 hours in seconds
                    .build();
            
            log.info("Token refresh successful for user: {}", username);
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Token refresh failed", "REFRESH_FAILED"));
        }
    }

    // Optional: normal user self-registration (signup)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody AuthenticationRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        // TODO: implement user creation + token generation
        return ResponseEntity.ok(ApiResponse.success(null, "Registration endpoint - implementation needed"));
    }
}

