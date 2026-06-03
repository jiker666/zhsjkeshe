package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.SystemSettingsDTO;
import com.course.securityagent.service.AuditLogService;
import com.course.securityagent.service.SystemSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemSettingsController {
    private final SystemSettingsService settingsService;
    private final AuditLogService auditLogService;

    public SystemSettingsController(SystemSettingsService settingsService, AuditLogService auditLogService) {
        this.settingsService = settingsService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/settings/public")
    public ApiResponse<SystemSettingsDTO> publicSettings() {
        return ApiResponse.ok(settingsService.getSettings());
    }

    @GetMapping("/admin/settings")
    public ApiResponse<SystemSettingsDTO> adminSettings() {
        requireAdmin();
        return ApiResponse.ok(settingsService.getSettings());
    }

    @PutMapping("/admin/settings")
    public ApiResponse<SystemSettingsDTO> save(@RequestBody SystemSettingsDTO request) {
        requireAdmin();
        SystemSettingsDTO saved = settingsService.saveSettings(request);
        auditLogService.record("UPDATE_SYSTEM_SETTINGS", "系统配置", "更新系统配置", "SUCCESS");
        return ApiResponse.ok(saved);
    }

    @PostMapping("/admin/settings/reset")
    public ApiResponse<SystemSettingsDTO> reset() {
        requireAdmin();
        SystemSettingsDTO saved = settingsService.resetDefaults();
        auditLogService.record("RESET_SYSTEM_SETTINGS", "系统配置", "恢复默认配置", "SUCCESS");
        return ApiResponse.ok(saved);
    }

    private void requireAdmin() {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }
}
