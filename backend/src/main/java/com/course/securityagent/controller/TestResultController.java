package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.mapper.TestResultMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import com.course.securityagent.service.AuditLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class TestResultController {
    private final TestResultMapper resultMapper;
    private final TestTaskMapper taskMapper;
    private final AuditLogService auditLogService;

    public TestResultController(TestResultMapper resultMapper, TestTaskMapper taskMapper, AuditLogService auditLogService) {
        this.resultMapper = resultMapper;
        this.taskMapper = taskMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long taskId,
                               @RequestParam(required = false) String riskLevel,
                               @RequestParam(required = false) String vulnerabilityType,
                               @RequestParam(required = false) Long page,
                               @RequestParam(required = false) Long size) {
        LambdaQueryWrapper<TestResult> wrapper = new LambdaQueryWrapper<TestResult>()
                .orderByDesc(TestResult::getCreatedAt);
        if (taskId != null) {
            wrapper.eq(TestResult::getTaskId, taskId);
        }
        if (riskLevel != null && !riskLevel.isBlank()) {
            wrapper.eq(TestResult::getRiskLevel, riskLevel);
        }
        if (vulnerabilityType != null && !vulnerabilityType.isBlank()) {
            wrapper.eq(TestResult::getVulnerabilityType, vulnerabilityType);
        }
        applyOwnerScope(wrapper);
        if (page != null || size != null) {
            long current = page == null || page < 1 ? 1 : page;
            long pageSize = size == null || size < 1 ? 10 : size;
            Page<TestResult> result = resultMapper.selectPage(new Page<>(current, pageSize), wrapper);
            return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
        }
        return ApiResponse.ok(resultMapper.selectList(wrapper));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TestResult>> page(@RequestParam(required = false) Long taskId,
                                                    @RequestParam(required = false) String riskLevel,
                                                    @RequestParam(required = false) String vulnerabilityType,
                                                    @RequestParam(defaultValue = "1") Long page,
                                                    @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<TestResult> wrapper = buildQuery(taskId, riskLevel, vulnerabilityType);
        long current = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : size;
        Page<TestResult> result = resultMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestResult> detail(@PathVariable Long id) {
        TestResult result = resultMapper.selectById(id);
        if (result == null) {
            throw new IllegalArgumentException("漏洞结果不存在");
        }
        ensureResultOwner(result);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<TestResult> updateStatus(@PathVariable Long id, @RequestParam String status) {
        TestResult result = resultMapper.selectById(id);
        if (result == null) {
            throw new IllegalArgumentException("漏洞结果不存在");
        }
        if (!List.of("OPEN", "CONFIRMED", "FIXED", "IGNORED").contains(status)) {
            throw new IllegalArgumentException("不支持的漏洞处理状态");
        }
        ensureResultOwner(result);
        if (!UserContext.isAdmin() && !List.of("CONFIRMED", "IGNORED").contains(status)) {
            throw new ForbiddenException("无权限访问该资源");
        }
        result.setStatus(status);
        resultMapper.updateById(result);
        auditLogService.record("UPDATE_RESULT_STATUS", "漏洞结果", "漏洞状态更新为：" + status + "，漏洞：" + result.getVulnerabilityName(), "SUCCESS");
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
        resultMapper.deleteById(id);
        auditLogService.record("DELETE_RESULT", "漏洞结果", "删除漏洞结果：" + id, "SUCCESS");
        return ApiResponse.ok();
    }

    private LambdaQueryWrapper<TestResult> buildQuery(Long taskId, String riskLevel, String vulnerabilityType) {
        LambdaQueryWrapper<TestResult> wrapper = new LambdaQueryWrapper<TestResult>()
                .orderByDesc(TestResult::getCreatedAt);
        if (taskId != null) {
            wrapper.eq(TestResult::getTaskId, taskId);
        }
        if (riskLevel != null && !riskLevel.isBlank()) {
            wrapper.eq(TestResult::getRiskLevel, riskLevel);
        }
        if (vulnerabilityType != null && !vulnerabilityType.isBlank()) {
            wrapper.eq(TestResult::getVulnerabilityType, vulnerabilityType);
        }
        applyOwnerScope(wrapper);
        return wrapper;
    }

    private void applyOwnerScope(LambdaQueryWrapper<TestResult> wrapper) {
        if (UserContext.isAdmin()) {
            return;
        }
        List<Long> taskIds = taskMapper.selectList(new LambdaQueryWrapper<TestTask>()
                        .eq(TestTask::getCreatedBy, UserContext.getUserId()))
                .stream()
                .map(TestTask::getId)
                .toList();
        if (taskIds.isEmpty()) {
            wrapper.eq(TestResult::getTaskId, -1L);
            return;
        }
        wrapper.in(TestResult::getTaskId, taskIds);
    }

    private void ensureResultOwner(TestResult result) {
        if (UserContext.isAdmin()) {
            return;
        }
        TestTask task = taskMapper.selectById(result.getTaskId());
        if (task == null || !UserContext.getUserId().equals(task.getCreatedBy())) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }
}
