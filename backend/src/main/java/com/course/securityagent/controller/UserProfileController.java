package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.PasswordUtil;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.PasswordUpdateRequest;
import com.course.securityagent.dto.ProfileUpdateRequest;
import com.course.securityagent.dto.UserDTO;
import com.course.securityagent.entity.User;
import com.course.securityagent.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {
    private final UserMapper userMapper;

    public UserProfileController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/profile")
    public ApiResponse<UserDTO> profile() {
        return ApiResponse.ok(UserDTO.from(currentUser()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserDTO> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User user = currentUser();
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(UserDTO.from(user));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestBody PasswordUpdateRequest request) {
        User user = currentUser();
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        if (!StringUtils.hasText(request.getNewPassword()) || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码至少 6 位");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码不一致");
        }
        user.setPassword(PasswordUtil.hash(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    private User currentUser() {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
}
