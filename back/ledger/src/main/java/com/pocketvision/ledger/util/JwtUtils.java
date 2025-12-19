package com.pocketvision.ledger.util;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value; // Import này thiếu
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    // Inject từ file properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    private Key getSignInKey() {
        // Lưu ý: Secret key cần đủ dài (>= 256 bit cho HS256)
        // Nếu secret key trong properties là Base64 string, decode trực tiếp
        // Nếu là plain string, encode sang bytes rồi dùng
        byte[] keyBytes;
        try {
            // Thử decode như Base64 string trước
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            // Nếu không phải Base64, dùng bytes trực tiếp từ string
            keyBytes = secretKey.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, Long userId, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId) // Lưu user ID vào token để tránh query database
                .claim("role", role) // Lưu role vào token để tránh query database
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Dùng biến đã inject
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Overload method để tương thích
    public String generateToken(String email, Long userId) {
        return generateToken(email, userId, null);
    }

    // Overload method để tương thích với code cũ (nếu có)
    public String generateToken(String email) {
        return generateToken(email, null, null);
    }

    // Trích xuất Role từ Token
    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            Object role = claims.get("role");
            return role != null ? role.toString() : null;
        });
    }

    // 3. Trích xuất Email từ Token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Trích xuất User ID từ Token (tối ưu performance - không cần query database)
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId == null) {
                return null;
            }
            if (userId instanceof Long) {
                return (Long) userId;
            }
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            return null;
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 4. Kiểm tra Token có hợp lệ không
    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}