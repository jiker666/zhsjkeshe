package com.course.securityagent.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, TokenSession> tokenStore = new ConcurrentHashMap<>();

    public String createToken(Long userId, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, new TokenSession(userId, role));
        return token;
    }

    public Long getUserId(String token) {
        TokenSession session = getSession(token);
        return session == null ? null : session.getUserId();
    }

    public String getRole(String token) {
        TokenSession session = getSession(token);
        return session == null ? null : session.getRole();
    }

    private TokenSession getSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenStore.get(token);
    }

    private static class TokenSession {
        private final Long userId;
        private final String role;

        private TokenSession(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }
    }
}
