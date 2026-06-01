package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.dto.TaskCreateRequest;
import com.course.securityagent.dto.TaskUpdateRequest;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.service.TestTaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TestTaskController {
    private final TestTaskService taskService;

    public TestTaskController(TestTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String testType,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String riskLevel,
                               @RequestParam(required = false) Long page,
                               @RequestParam(required = false) Long size) {
        if (page != null || size != null) {
            long current = page == null || page < 1 ? 1 : page;
            long pageSize = size == null || size < 1 ? 10 : size;
            PageResult<TestTask> result = taskService.pageTasks(keyword, testType, status, riskLevel, current, pageSize);
            return ApiResponse.ok(result);
        }
        return ApiResponse.ok(taskService.listTasks(keyword, status));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TestTask>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String testType,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String riskLevel,
                                                  @RequestParam(defaultValue = "1") Long page,
                                                  @RequestParam(defaultValue = "10") Long size) {
        long current = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : size;
        return ApiResponse.ok(taskService.pageTasks(keyword, testType, status, riskLevel, current, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestTask> detail(@PathVariable Long id) {
        return ApiResponse.ok(taskService.getTask(id));
    }

    @PostMapping
    public ApiResponse<TestTask> create(@RequestBody TaskCreateRequest request) {
        return ApiResponse.ok(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TestTask> update(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        return ApiResponse.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<TestTask> execute(@PathVariable Long id) {
        return ApiResponse.ok(taskService.executeTask(id));
    }
}
