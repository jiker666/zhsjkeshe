package com.course.securityagent.dto;

public class SystemSettingsDTO {
    private String platformName;
    private String agentMode;
    private String defaultDepth;
    private Boolean autoReport;
    private Boolean enableRegister;
    private String statement;

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getAgentMode() {
        return agentMode;
    }

    public void setAgentMode(String agentMode) {
        this.agentMode = agentMode;
    }

    public String getDefaultDepth() {
        return defaultDepth;
    }

    public void setDefaultDepth(String defaultDepth) {
        this.defaultDepth = defaultDepth;
    }

    public Boolean getAutoReport() {
        return autoReport;
    }

    public void setAutoReport(Boolean autoReport) {
        this.autoReport = autoReport;
    }

    public Boolean getEnableRegister() {
        return enableRegister;
    }

    public void setEnableRegister(Boolean enableRegister) {
        this.enableRegister = enableRegister;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}
