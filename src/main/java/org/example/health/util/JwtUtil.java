package org.example.health.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "health-demo-secret-key-1234567890";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Long userId, String username, Integer role) {
        long now = System.currentTimeMillis();
        long expire = now + 7L * 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role == null ? 0 : role)
                .issuedAt(new Date(now))
                .expiration(new Date(expire))
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long parseUserId(String token) {
        Claims claims = parseToken(token);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }
        return Long.parseLong(subject);
    }

    public static String parseUsername(String token) {
        Claims claims = parseToken(token);
        Object username = claims.get("username");
        return username == null ? null : String.valueOf(username);
    }

    public static Integer parseRole(String token) {
        Claims claims = parseToken(token);
        Object role = claims.get("role");
        if (role == null) {
            return 0;
        }
        if (role instanceof Integer) {
            return (Integer) role;
        }
        if (role instanceof Number) {
            return ((Number) role).intValue();
        }
        return Integer.parseInt(String.valueOf(role));
    }

    public static boolean isExpired(String token) {
        Claims claims = parseToken(token);
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }

    public static Long getUserIdFromHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();
        try {
            return parseUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getRoleFromHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();
        try {
            return parseRole(token);
        } catch (Exception e) {
            return null;
        }
    }
}