package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.mapper.TestLogMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class TestLogController {
    private final TestLogMapper logMapper;
    private final TestTaskMapper taskMapper;

    public TestLogController(TestLogMapper logMapper, TestTaskMapper taskMapper) {
        this.logMapper = logMapper;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long taskId,
                               @RequestParam(required = false) String logLevel,
                               @RequestParam(required = false) Long page,
                               @RequestParam(required = false) Long size) {
        LambdaQueryWrapper<TestLog> wrapper = new LambdaQueryWrapper<TestLog>()
                .orderByDesc(TestLog::getCreatedAt);
        if (taskId != null) {
            wrapper.eq(TestLog::getTaskId, taskId);
        }
        if (logLevel != null && !logLevel.isBlank()) {
            wrapper.eq(TestLog::getLogLevel, logLevel);
        }
        applyOwnerScope(wrapper);
        if (page != null || size != null) {
            long current = page == null || page < 1 ? 1 : page;
            long pageSize = size == null || size < 1 ? 10 : size;
            Page<TestLog> result = logMapper.selectPage(new Page<>(current, pageSize), wrapper);
            return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
        }
        return ApiResponse.ok(logMapper.selectList(wrapper));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TestLog>> page(@RequestParam(required = false) Long taskId,
                                                 @RequestParam(required = false) String logLevel,
                                                 @RequestParam(defaultValue = "1") Long page,
                                                 @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<TestLog> wrapper = new LambdaQueryWrapper<TestLog>()
                .orderByDesc(TestLog::getCreatedAt);
        if (taskId != null) {
            wrapper.eq(TestLog::getTaskId, taskId);
        }
        if (logLevel != null && !logLevel.isBlank()) {
            wrapper.eq(TestLog::getLogLevel, logLevel);
        }
        applyOwnerScope(wrapper);
        long current = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : size;
        Page<TestLog> result = logMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    private void applyOwnerScope(LambdaQueryWrapper<TestLog> wrapper) {
        if (UserContext.isAdmin()) {
            return;
        }
        List<Long> taskIds = taskMapper.selectList(new LambdaQueryWrapper<TestTask>()
                        .eq(TestTask::getCreatedBy, UserContext.getUserId()))
                .stream()
                .map(TestTask::getId)
                .toList();
        if (taskIds.isEmpty()) {
            wrapper.eq(TestLog::getTaskId, -1L);
            return;
        }
        wrapper.in(TestLog::getTaskId, taskIds);
    }
}
