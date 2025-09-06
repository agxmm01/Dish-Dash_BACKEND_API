package in.agampal.dishdashapi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long JWT_EXPIRATION;

    @Value("${jwt.refresh.expiration:604800000}") // 7 days default
    private long REFRESH_TOKEN_EXPIRATION;

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), JWT_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_EXPIRATION);
    }

    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, JWT_EXPIRATION);
    }

    protected String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error extracting claim from token", e);
            throw new RuntimeException("Error extracting claim from token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired at: {}, current time: {}", e.getClaims().getExpiration(), new Date());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported", e);
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed", e);
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SignatureException e) {
            log.error("JWT signature validation failed", e);
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid", e);
            throw new RuntimeException("JWT token compact of handler are invalid", e);
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    // ⬇️ Changed to public
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiration", e);
            return true; // Consider token expired if we can't parse it
        }
    }

    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isTokenExpiredException(Exception e) {
        return e instanceof ExpiredJwtException;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (token == null || userDetails == null) {
                return false;
            }
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            log.warn("Token expired for user: {}", userDetails != null ? userDetails.getUsername() : "unknown");
            return false;
        } catch (Exception e) {
            log.error("Token validation error", e);
            return false; // Return false for any validation error
        }
    }

    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        try {
            if (token == null || userDetails == null) {
                return false;
            }
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token) &&
                    isRefreshToken(token));
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token expired for user: {}", userDetails != null ? userDetails.getUsername() : "unknown");
            return false;
        } catch (Exception e) {
            log.error("Refresh token validation error", e);
            return false;
        }
    }
}
