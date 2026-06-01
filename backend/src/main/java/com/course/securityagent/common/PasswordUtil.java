package com.course.securityagent.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("密码加密组件不可用", exception);
        }
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return storedPassword.equals(hash(rawPassword)) || storedPassword.equals(rawPassword);
    }
}
