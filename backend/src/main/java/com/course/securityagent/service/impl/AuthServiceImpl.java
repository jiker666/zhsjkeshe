package com.course.securityagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.securityagent.common.PasswordUtil;
import com.course.securityagent.dto.LoginRequest;
import com.course.securityagent.dto.LoginResponse;
import com.course.securityagent.dto.RegisterRequest;
import com.course.securityagent.dto.UserDTO;
import com.course.securityagent.entity.User;
import com.course.securityagent.mapper.UserMapper;
import com.course.securityagent.service.AuditLogService;
import com.course.securityagent.service.AuthService;
import com.course.securityagent.service.SystemSettingsService;
import com.course.securityagent.service.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final AuditLogService auditLogService;
    private final SystemSettingsService settingsService;

    public AuthServiceImpl(UserMapper userMapper,
                           TokenService tokenService,
                           AuditLogService auditLogService,
                           SystemSettingsService settingsService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
        this.auditLogService = auditLogService;
        this.settingsService = settingsService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null || !isEnabled(user) || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            auditLogService.record(null, request == null ? "unknown" : request.getUsername(), "-", "LOGIN", "认证中心", "用户登录失败", "FAILED");
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = tokenService.createToken(user.getId(), user.getRole());
        auditLogService.record(user.getId(), user.getUsername(), user.getRole(), "LOGIN", "认证中心", "用户登录成功", "SUCCESS");
        return new LoginResponse(token, UserDTO.from(user));
    }

    @Override
    public UserDTO register(RegisterRequest request) {
        if (!settingsService.isRegisterEnabled()) {
            auditLogService.record(null, request == null ? "unknown" : request.getUsername(), "-", "REGISTER", "认证中心", "系统已关闭注册", "FAILED");
            throw new IllegalArgumentException("系统已关闭注册，请联系管理员创建账号");
        }
        validateRegister(request);
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(PasswordUtil.hash(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus("ENABLED");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        auditLogService.record(user.getId(), user.getUsername(), user.getRole(), "REGISTER", "认证中心", "用户注册成功", "SUCCESS");
        return UserDTO.from(user);
    }

    private void validateRegister(RegisterRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码不一致");
        }
        if (!StringUtils.hasText(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }

    private boolean isEnabled(User user) {
        return "ENABLED".equals(user.getStatus()) || "1".equals(user.getStatus());
    }
}
