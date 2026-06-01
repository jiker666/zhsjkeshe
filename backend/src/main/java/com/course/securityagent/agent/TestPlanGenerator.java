package com.course.securityagent.agent;

public interface TestPlanGenerator {
    String generate(String targetUrl, String testType, String description, String testDepth);
}
