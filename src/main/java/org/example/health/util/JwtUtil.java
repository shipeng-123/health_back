package org.example.health.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // 至少32位
    private static final String SECRET = "health-demo-secret-key-1234567890";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Long userId, String username) {
        long now = System.currentTimeMillis();
        long expire = now + 7L * 24 * 60 * 60 * 1000; // 7天

        return Jwts.builder()
                .subject(String.valueOf(userId))   // 把 userId 放在 subject
                .claim("username", username)
                .issuedAt(new Date(now))
                .expiration(new Date(expire))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 token，返回 Claims（内部通用）
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 中解析 userId
     * 这里 userId 存在 subject 里（generateToken 时设置的）
     */
    public static Long parseUserId(String token) {
        Claims claims = parseToken(token);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }
        return Long.parseLong(subject);
    }

    /**
     * 从 token 中解析 username（可选，后续可能会用到）
     */
    public static String parseUsername(String token) {
        Claims claims = parseToken(token);
        Object username = claims.get("username");
        return username == null ? null : String.valueOf(username);
    }

    /**
     * 判断 token 是否过期（可选）
     */
    public static boolean isExpired(String token) {
        Claims claims = parseToken(token);
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }
}