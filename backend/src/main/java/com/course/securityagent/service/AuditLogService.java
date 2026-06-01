package com.course.securityagent.service;

import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.AuditLog;
import com.course.securityagent.entity.User;
import com.course.securityagent.mapper.AuditLogMapper;
import com.course.securityagent.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

    public AuditLogService(AuditLogMapper auditLogMapper, UserMapper userMapper) {
        this.auditLogMapper = auditLogMapper;
        this.userMapper = userMapper;
    }

    public void record(String action, String module, String detail, String result) {
        Long userId = UserContext.getUserId();
        User user = userId == null ? null : userMapper.selectById(userId);
        String username = user == null ? "anonymous" : user.getUsername();
        String role = user == null ? UserContext.getRole() : user.getRole();
        record(userId, username, role, action, module, detail, result);
    }

    public void record(Long userId, String username, String role, String action, String module, String detail, String result) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username == null ? "anonymous" : username);
        log.setRole(role == null ? "-" : role);
        log.setAction(action);
        log.setModule(module);
        log.setDetail(detail);
        log.setIp("local");
        log.setResult(result == null ? "SUCCESS" : result);
        log.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
