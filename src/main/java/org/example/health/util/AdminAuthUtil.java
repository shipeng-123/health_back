package org.example.health.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthUtil {

    public Long requireAdmin(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        try {
            Integer role = JwtUtil.getRoleFromHeader(authorization);
            Long userId = JwtUtil.getUserIdFromHeader(authorization);
            if (role == null || role != 1 || userId == null) {
                return null;
            }
            return userId;
        } catch (Exception e) {
            return null;
        }
    }
}