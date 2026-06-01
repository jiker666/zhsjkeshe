package com.course.securityagent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("test_result")
public class TestResult {
    private Long id;
    private Long taskId;
    private String vulnerabilityName;
    private String vulnerabilityType;
    private String riskLevel;
    private String url;
    private String description;
    private String reproduceSteps;
    private String requestExample;
    private String responseExample;
    private String suggestion;
    private String status;
    private Double riskScore;
    private Double impactScore;
    private Double exploitabilityScore;
    private Double difficultyScore;
    private String riskVector;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getVulnerabilityName() {
        return vulnerabilityName;
    }

    public void setVulnerabilityName(String vulnerabilityName) {
        this.vulnerabilityName = vulnerabilityName;
    }

    public String getVulnerabilityType() {
        return vulnerabilityType;
    }

    public void setVulnerabilityType(String vulnerabilityType) {
        this.vulnerabilityType = vulnerabilityType;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReproduceSteps() {
        return reproduceSteps;
    }

    public void setReproduceSteps(String reproduceSteps) {
        this.reproduceSteps = reproduceSteps;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(Double impactScore) {
        this.impactScore = impactScore;
    }

    public Double getExploitabilityScore() {
        return exploitabilityScore;
    }

    public void setExploitabilityScore(Double exploitabilityScore) {
        this.exploitabilityScore = exploitabilityScore;
    }

    public Double getDifficultyScore() {
        return difficultyScore;
    }

    public void setDifficultyScore(Double difficultyScore) {
        this.difficultyScore = difficultyScore;
    }

    public String getRiskVector() {
        return riskVector;
    }

    public void setRiskVector(String riskVector) {
        this.riskVector = riskVector;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
