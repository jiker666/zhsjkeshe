package com.course.securityagent.agent;

import com.course.securityagent.entity.TestTask;

public interface SecurityAgentService {
    String buildPlan(String targetUrl, String testType, String description);

    String buildPlan(String targetUrl, String testType, String description, String testDepth);

    AgentExecutionResult execute(TestTask task);
}
