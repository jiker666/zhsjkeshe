package com.course.securityagent.service;

import com.course.securityagent.common.PageResult;
import com.course.securityagent.dto.TaskCreateRequest;
import com.course.securityagent.dto.TaskUpdateRequest;
import com.course.securityagent.entity.TestTask;

import java.util.List;

public interface TestTaskService {
    List<TestTask> listTasks(String keyword, String status);

    PageResult<TestTask> pageTasks(String keyword, String testType, String status, String riskLevel, long page, long size);

    TestTask getTask(Long id);

    TestTask createTask(TaskCreateRequest request);

    TestTask updateTask(Long id, TaskUpdateRequest request);

    void deleteTask(Long id);

    TestTask executeTask(Long id);
}
