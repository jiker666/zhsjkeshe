package com.course.securityagent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("security_knowledge")
public class SecurityKnowledge {
    private Long id;
    private String vulnType;
    private String title;
    private String riskLevel;
    private String description;
    private String cause;
    private String impact;
    private String fixAdvice;
    private String reference;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVulnType() {
        return vulnType;
    }

    public void setVulnType(String vulnType) {
        this.vulnType = vulnType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getFixAdvice() {
        return fixAdvice;
    }

    public void setFixAdvice(String fixAdvice) {
        this.fixAdvice = fixAdvice;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
