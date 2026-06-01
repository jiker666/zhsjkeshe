package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.PasswordUtil;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.UserDTO;
import com.course.securityagent.dto.UserSaveRequest;
import com.course.securityagent.entity.User;
import com.course.securityagent.mapper.UserMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<UserDTO>> page(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String role,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") Long page,
                                                 @RequestParam(defaultValue = "10") Long size) {
        requireAdmin();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(User::getStatus, status);
        }
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserDTO> records = result.getRecords().stream().map(UserDTO::from).collect(Collectors.toList());
        return ApiResponse.ok(new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping
    public ApiResponse<UserDTO> create(@RequestBody UserSaveRequest request) {
        requireAdmin();
        validateUser(request, true);
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        copy(request, user);
        user.setPassword(PasswordUtil.hash(request.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return ApiResponse.ok(UserDTO.from(user));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDTO> update(@PathVariable Long id, @RequestBody UserSaveRequest request) {
        requireAdmin();
        User user = getUser(id);
        copy(request, user);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(UserDTO.from(user));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        requireAdmin();
        User user = getUser(id);
        if (user.getId().equals(UserContext.getUserId()) && "DISABLED".equals(status)) {
            throw new IllegalArgumentException("不能禁用当前登录管理员");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(UserDTO.from(user));
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody UserSaveRequest request) {
        requireAdmin();
        User user = getUser(id);
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        user.setPassword(PasswordUtil.hash(request.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireAdmin();
        if (id.equals(UserContext.getUserId())) {
            throw new IllegalArgumentException("不能删除当前登录管理员");
        }
        userMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void validateUser(UserSaveRequest request, boolean requirePassword) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (requirePassword && (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6)) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
    }

    private void copy(UserSaveRequest request, User user) {
        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(StringUtils.hasText(request.getRole()) ? request.getRole() : "USER");
        user.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
    }

    private User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private void requireAdmin() {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }
}
