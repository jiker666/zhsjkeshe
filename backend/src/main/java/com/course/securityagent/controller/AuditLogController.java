package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.AuditLog;
import com.course.securityagent.mapper.AuditLogMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {
    private final AuditLogMapper auditLogMapper;

    public AuditLogController(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<AuditLog>> page(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String module,
                                                  @RequestParam(required = false) String action,
                                                  @RequestParam(required = false) String result,
                                                  @RequestParam(defaultValue = "1") Long page,
                                                  @RequestParam(defaultValue = "10") Long size) {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<AuditLog>()
                .orderByDesc(AuditLog::getCreateTime);
        if (StringUtils.hasText(username)) {
            wrapper.like(AuditLog::getUsername, username);
        }
        if (StringUtils.hasText(module)) {
            wrapper.like(AuditLog::getModule, module);
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(AuditLog::getAction, action);
        }
        if (StringUtils.hasText(result)) {
            wrapper.eq(AuditLog::getResult, result);
        }
        Page<AuditLog> logs = auditLogMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResult<>(logs.getRecords(), logs.getTotal(), logs.getCurrent(), logs.getSize()));
    }
}
