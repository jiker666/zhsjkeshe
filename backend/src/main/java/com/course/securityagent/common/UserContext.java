package com.course.securityagent.common;

public final class UserContext {
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static void setRole(String role) {
        CURRENT_ROLE.set(role);
    }

    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    public static String getRole() {
        return CURRENT_ROLE.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(CURRENT_ROLE.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_ROLE.remove();
    }
}
