package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.entity.AgentDecision;
import com.course.securityagent.mapper.AgentDecisionMapper;
import com.course.securityagent.service.TestTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class AgentDecisionController {
    private final AgentDecisionMapper decisionMapper;
    private final TestTaskService taskService;

    public AgentDecisionController(AgentDecisionMapper decisionMapper, TestTaskService taskService) {
        this.decisionMapper = decisionMapper;
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}/agent-decisions")
    public ApiResponse<List<AgentDecision>> list(@PathVariable Long taskId) {
        taskService.getTask(taskId);
        return ApiResponse.ok(decisionMapper.selectList(new LambdaQueryWrapper<AgentDecision>()
                .eq(AgentDecision::getTaskId, taskId)
                .orderByAsc(AgentDecision::getStepNo)));
    }
}
