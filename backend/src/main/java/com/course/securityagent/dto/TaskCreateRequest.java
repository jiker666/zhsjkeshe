package com.course.securityagent.dto;

public class TaskCreateRequest {
    private String taskName;
    private String targetUrl;
    private String testType;
    private String description;
    private String testDepth;
    private Integer generateReport;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestDepth() {
        return testDepth;
    }

    public void setTestDepth(String testDepth) {
        this.testDepth = testDepth;
    }

    public Integer getGenerateReport() {
        return generateReport;
    }

    public void setGenerateReport(Integer generateReport) {
        this.generateReport = generateReport;
    }
}
