package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.RemediationTicket;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.User;
import com.course.securityagent.mapper.RemediationTicketMapper;
import com.course.securityagent.mapper.TestResultMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import com.course.securityagent.mapper.UserMapper;
import com.course.securityagent.service.AuditLogService;
import org.springframework.util.StringUtils;
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

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private static final List<String> STATUSES = List.of("TODO", "CONFIRMED", "FIXING", "RETESTING", "CLOSED", "IGNORED");
    private final RemediationTicketMapper ticketMapper;
    private final TestResultMapper resultMapper;
    private final TestTaskMapper taskMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    public TicketController(RemediationTicketMapper ticketMapper,
                            TestResultMapper resultMapper,
                            TestTaskMapper taskMapper,
                            UserMapper userMapper,
                            AuditLogService auditLogService) {
        this.ticketMapper = ticketMapper;
        this.resultMapper = resultMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<RemediationTicket>> page(@RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String riskLevel,
                                                           @RequestParam(required = false) Long assigneeId,
                                                           @RequestParam(defaultValue = "1") Long page,
                                                           @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<RemediationTicket> wrapper = new LambdaQueryWrapper<RemediationTicket>()
                .orderByDesc(RemediationTicket::getUpdateTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(RemediationTicket::getStatus, status);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(RemediationTicket::getRiskLevel, riskLevel);
        }
        if (assigneeId != null) {
            wrapper.eq(RemediationTicket::getAssigneeId, assigneeId);
        }
        applyOwnerScope(wrapper);
        Page<RemediationTicket> result = ticketMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping("/from-result/{resultId}")
    public ApiResponse<RemediationTicket> createFromResult(@PathVariable Long resultId) {
        TestResult result = resultMapper.selectById(resultId);
        if (result == null) {
            throw new IllegalArgumentException("漏洞结果不存在");
        }
        TestTask task = ensureTaskOwner(result.getTaskId());
        RemediationTicket exists = ticketMapper.selectOne(new LambdaQueryWrapper<RemediationTicket>()
                .eq(RemediationTicket::getResultId, resultId)
                .last("limit 1"));
        if (exists != null) {
            return ApiResponse.ok(exists);
        }
        User assignee = task.getCreatedBy() == null ? null : userMapper.selectById(task.getCreatedBy());
        RemediationTicket ticket = new RemediationTicket();
        ticket.setResultId(result.getId());
        ticket.setTaskId(result.getTaskId());
        ticket.setTitle("整改工单：" + result.getVulnerabilityName());
        ticket.setRiskLevel(result.getRiskLevel());
        ticket.setStatus("TODO");
        ticket.setAssigneeId(task.getCreatedBy());
        ticket.setAssigneeName(assignee == null ? task.getCreateUsername() : assignee.getNickname());
        ticket.setFixSuggestion(result.getSuggestion());
        ticket.setRetestResult("尚未复测");
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.insert(ticket);
        auditLogService.record("CREATE_TICKET", "整改工单", "从漏洞生成工单：" + result.getVulnerabilityName(), "SUCCESS");
        return ApiResponse.ok(ticket);
    }

    @PutMapping("/{id}")
    public ApiResponse<RemediationTicket> update(@PathVariable Long id, @RequestBody RemediationTicket request) {
        RemediationTicket ticket = getOwnedTicket(id);
        if (StringUtils.hasText(request.getTitle())) {
            ticket.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getFixSuggestion())) {
            ticket.setFixSuggestion(request.getFixSuggestion());
        }
        if (UserContext.isAdmin()) {
            ticket.setAssigneeId(request.getAssigneeId());
            ticket.setAssigneeName(request.getAssigneeName());
        }
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        auditLogService.record("EDIT_TICKET", "整改工单", "编辑工单：" + ticket.getTitle(), "SUCCESS");
        return ApiResponse.ok(ticket);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<RemediationTicket> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的工单状态");
        }
        RemediationTicket ticket = getOwnedTicket(id);
        ticket.setStatus(status);
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        auditLogService.record("UPDATE_TICKET_STATUS", "整改工单", "工单状态更新为：" + status, "SUCCESS");
        return ApiResponse.ok(ticket);
    }

    @PostMapping("/{id}/retest")
    public ApiResponse<RemediationTicket> retest(@PathVariable Long id) {
        RemediationTicket ticket = getOwnedTicket(id);
        TestResult result = resultMapper.selectById(ticket.getResultId());
        if (result != null && "FIXED".equals(result.getStatus())) {
            ticket.setStatus("CLOSED");
            ticket.setRetestResult("复测通过：关联漏洞已标记为已修复，整改闭环完成。");
        } else {
            ticket.setStatus("RETESTING");
            ticket.setRetestResult("复测未完全通过：关联漏洞尚未标记为已修复，仍需继续整改。");
        }
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        auditLogService.record("RETEST_TICKET", "整改工单", "执行工单复测：" + ticket.getTitle(), "SUCCESS");
        return ApiResponse.ok(ticket);
    }

    private void applyOwnerScope(LambdaQueryWrapper<RemediationTicket> wrapper) {
        if (UserContext.isAdmin()) {
            return;
        }
        List<Long> taskIds = taskMapper.selectList(new LambdaQueryWrapper<TestTask>()
                        .eq(TestTask::getCreatedBy, UserContext.getUserId()))
                .stream()
                .map(TestTask::getId)
                .toList();
        if (taskIds.isEmpty()) {
            wrapper.eq(RemediationTicket::getTaskId, -1L);
            return;
        }
        wrapper.in(RemediationTicket::getTaskId, taskIds);
    }

    private RemediationTicket getOwnedTicket(Long id) {
        RemediationTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new IllegalArgumentException("整改工单不存在");
        }
        ensureTaskOwner(ticket.getTaskId());
        return ticket;
    }

    private TestTask ensureTaskOwner(Long taskId) {
        TestTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("测试任务不存在");
        }
        if (!UserContext.isAdmin() && !UserContext.getUserId().equals(task.getCreatedBy())) {
            throw new ForbiddenException("无权限访问该资源");
        }
        return task;
    }
}
