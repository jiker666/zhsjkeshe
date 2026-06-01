package com.course.securityagent.dto;

import com.course.securityagent.entity.Challenge;

import java.time.LocalDateTime;

public class ChallengeDTO {
    private Long id;
    private String title;
    private String category;
    private String difficulty;
    private Integer score;
    private String description;
    private String targetUrl;
    private String requestMethod;
    private String requestExample;
    private String explanation;
    private Long knowledgeId;
    private String status;
    private Long createUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean solved;
    private long submitCount;
    private long correctCount;
    private double passRate;

    public static ChallengeDTO from(Challenge challenge, boolean solved, long submitCount, long correctCount, boolean showExplanation) {
        ChallengeDTO dto = new ChallengeDTO();
        dto.setId(challenge.getId());
        dto.setTitle(challenge.getTitle());
        dto.setCategory(challenge.getCategory());
        dto.setDifficulty(challenge.getDifficulty());
        dto.setScore(challenge.getScore());
        dto.setDescription(challenge.getDescription());
        dto.setTargetUrl(challenge.getTargetUrl());
        dto.setRequestMethod(challenge.getRequestMethod());
        dto.setRequestExample(challenge.getRequestExample());
        dto.setExplanation(showExplanation ? challenge.getExplanation() : null);
        dto.setKnowledgeId(challenge.getKnowledgeId());
        dto.setStatus(challenge.getStatus());
        dto.setCreateUserId(challenge.getCreateUserId());
        dto.setCreateTime(challenge.getCreateTime());
        dto.setUpdateTime(challenge.getUpdateTime());
        dto.setSolved(solved);
        dto.setSubmitCount(submitCount);
        dto.setCorrectCount(correctCount);
        dto.setPassRate(submitCount == 0 ? 0.0 : Math.round(correctCount * 1000.0 / submitCount) / 10.0);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public long getSubmitCount() {
        return submitCount;
    }

    public void setSubmitCount(long submitCount) {
        this.submitCount = submitCount;
    }

    public long getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(long correctCount) {
        this.correctCount = correctCount;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }
}
