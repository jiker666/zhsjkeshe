package com.course.securityagent.dto;

import com.course.securityagent.entity.User;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createTime;

    public static UserDTO from(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(normalizeStatus(user.getStatus()));
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }

    private static String normalizeStatus(String status) {
        return "1".equals(status) ? "ENABLED" : "0".equals(status) ? "DISABLED" : status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
