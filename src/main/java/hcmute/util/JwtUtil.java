// File: JwtUtil.java
package hcmute.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 1. Tạo Key ký JWT (Bản 0.12.x sử dụng SecretKey)
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 2. Sinh Token
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims) // Cú pháp mới của 0.12.x
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey()) // Tự động nhận diện thuật toán mã hóa
                .compact();
    }

    // 3. Trích xuất Email (Subject)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 4. Kiểm tra tính hợp lệ
    public boolean validateToken(String token) {
        try {
            Jwts.parser() // Thay thế cho parserBuilder()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            System.err.println("Token không hợp lệ (Sai định dạng/Chữ ký): " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Token đã hết hạn: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Token không được hỗ trợ: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Chuỗi Token trống: " + e.getMessage());
        }
        return false;
    }

    // --- Các hàm hỗ trợ nội bộ ---
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Bản 0.12.x dùng getPayload() thay cho getBody()
    }
}