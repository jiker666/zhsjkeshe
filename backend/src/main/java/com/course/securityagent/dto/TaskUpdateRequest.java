package com.course.securityagent.dto;

public class TaskUpdateRequest {
    private String taskName;
    private String targetUrl;
    private String testType;
    private String description;
    private String testDepth;
    private Integer generateReport;
    private String status;

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

    public void setGenerateReport(Object generateReport) {
        this.generateReport = parseGenerateReport(generateReport);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private Integer parseGenerateReport(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        if (value instanceof Number number) {
            return number.intValue() == 0 ? 0 : 1;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if ("true".equalsIgnoreCase(text)) {
            return 1;
        }
        if ("false".equalsIgnoreCase(text)) {
            return 0;
        }
        return Integer.parseInt(text) == 0 ? 0 : 1;
    }
}
