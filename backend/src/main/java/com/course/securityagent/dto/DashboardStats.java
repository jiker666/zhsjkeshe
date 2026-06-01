package com.course.securityagent.dto;

import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.AuditLog;
import com.course.securityagent.entity.RemediationTicket;
import com.course.securityagent.entity.ChallengeSubmit;
import com.course.securityagent.entity.UserScore;

import java.util.List;
import java.util.Map;

public class DashboardStats {
    private long totalTasks;
    private long runningTasks;
    private long completedTasks;
    private long totalVulnerabilities;
    private long highRiskCount;
    private long openVulnerabilities;
    private long todayTests;
    private long totalUsers;
    private long pendingTickets;
    private long closedTickets;
    private long totalReports;
    private long todayAuditCount;
    private long agentDecisionCount;
    private long challengeCount;
    private long todayChallengeSubmits;
    private long myChallengeScore;
    private long myChallengeSolved;
    private long myAchievementCount;
    private double ticketClosureRate;
    private double averageRiskScore;
    private double maxRiskScore;
    private double challengePassRate;
    private Map<String, Long> riskDistribution;
    private Map<String, Long> taskTrend;
    private Map<String, Long> testTypeDistribution;
    private Map<String, Long> taskStatusDistribution;
    private Map<String, Long> ticketStatusDistribution;
    private List<TestTask> recentTasks;
    private List<TestResult> highRiskTop;
    private List<TestResult> riskScoreTop;
    private List<RemediationTicket> recentTickets;
    private List<AuditLog> recentAuditLogs;
    private List<UserScore> scoreTop;
    private List<ChallengeSubmit> recentChallengeSubmissions;

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(long runningTasks) {
        this.runningTasks = runningTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getTotalVulnerabilities() {
        return totalVulnerabilities;
    }

    public void setTotalVulnerabilities(long totalVulnerabilities) {
        this.totalVulnerabilities = totalVulnerabilities;
    }

    public long getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(long highRiskCount) {
        this.highRiskCount = highRiskCount;
    }

    public long getOpenVulnerabilities() {
        return openVulnerabilities;
    }

    public void setOpenVulnerabilities(long openVulnerabilities) {
        this.openVulnerabilities = openVulnerabilities;
    }

    public long getTodayTests() {
        return todayTests;
    }

    public void setTodayTests(long todayTests) {
        this.todayTests = todayTests;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getPendingTickets() {
        return pendingTickets;
    }

    public void setPendingTickets(long pendingTickets) {
        this.pendingTickets = pendingTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getTodayAuditCount() {
        return todayAuditCount;
    }

    public void setTodayAuditCount(long todayAuditCount) {
        this.todayAuditCount = todayAuditCount;
    }

    public long getAgentDecisionCount() {
        return agentDecisionCount;
    }

    public void setAgentDecisionCount(long agentDecisionCount) {
        this.agentDecisionCount = agentDecisionCount;
    }

    public long getChallengeCount() {
        return challengeCount;
    }

    public void setChallengeCount(long challengeCount) {
        this.challengeCount = challengeCount;
    }

    public long getTodayChallengeSubmits() {
        return todayChallengeSubmits;
    }

    public void setTodayChallengeSubmits(long todayChallengeSubmits) {
        this.todayChallengeSubmits = todayChallengeSubmits;
    }

    public long getMyChallengeScore() {
        return myChallengeScore;
    }

    public void setMyChallengeScore(long myChallengeScore) {
        this.myChallengeScore = myChallengeScore;
    }

    public long getMyChallengeSolved() {
        return myChallengeSolved;
    }

    public void setMyChallengeSolved(long myChallengeSolved) {
        this.myChallengeSolved = myChallengeSolved;
    }

    public long getMyAchievementCount() {
        return myAchievementCount;
    }

    public void setMyAchievementCount(long myAchievementCount) {
        this.myAchievementCount = myAchievementCount;
    }

    public double getTicketClosureRate() {
        return ticketClosureRate;
    }

    public void setTicketClosureRate(double ticketClosureRate) {
        this.ticketClosureRate = ticketClosureRate;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(double averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }

    public double getMaxRiskScore() {
        return maxRiskScore;
    }

    public void setMaxRiskScore(double maxRiskScore) {
        this.maxRiskScore = maxRiskScore;
    }

    public double getChallengePassRate() {
        return challengePassRate;
    }

    public void setChallengePassRate(double challengePassRate) {
        this.challengePassRate = challengePassRate;
    }

    public Map<String, Long> getRiskDistribution() {
        return riskDistribution;
    }

    public void setRiskDistribution(Map<String, Long> riskDistribution) {
        this.riskDistribution = riskDistribution;
    }

    public Map<String, Long> getTaskTrend() {
        return taskTrend;
    }

    public void setTaskTrend(Map<String, Long> taskTrend) {
        this.taskTrend = taskTrend;
    }

    public Map<String, Long> getTestTypeDistribution() {
        return testTypeDistribution;
    }

    public void setTestTypeDistribution(Map<String, Long> testTypeDistribution) {
        this.testTypeDistribution = testTypeDistribution;
    }

    public Map<String, Long> getTaskStatusDistribution() {
        return taskStatusDistribution;
    }

    public void setTaskStatusDistribution(Map<String, Long> taskStatusDistribution) {
        this.taskStatusDistribution = taskStatusDistribution;
    }

    public Map<String, Long> getTicketStatusDistribution() {
        return ticketStatusDistribution;
    }

    public void setTicketStatusDistribution(Map<String, Long> ticketStatusDistribution) {
        this.ticketStatusDistribution = ticketStatusDistribution;
    }

    public List<TestTask> getRecentTasks() {
        return recentTasks;
    }

    public void setRecentTasks(List<TestTask> recentTasks) {
        this.recentTasks = recentTasks;
    }

    public List<TestResult> getHighRiskTop() {
        return highRiskTop;
    }

    public void setHighRiskTop(List<TestResult> highRiskTop) {
        this.highRiskTop = highRiskTop;
    }

    public List<TestResult> getRiskScoreTop() {
        return riskScoreTop;
    }

    public void setRiskScoreTop(List<TestResult> riskScoreTop) {
        this.riskScoreTop = riskScoreTop;
    }

    public List<RemediationTicket> getRecentTickets() {
        return recentTickets;
    }

    public void setRecentTickets(List<RemediationTicket> recentTickets) {
        this.recentTickets = recentTickets;
    }

    public List<AuditLog> getRecentAuditLogs() {
        return recentAuditLogs;
    }

    public void setRecentAuditLogs(List<AuditLog> recentAuditLogs) {
        this.recentAuditLogs = recentAuditLogs;
    }

    public List<UserScore> getScoreTop() {
        return scoreTop;
    }

    public void setScoreTop(List<UserScore> scoreTop) {
        this.scoreTop = scoreTop;
    }

    public List<ChallengeSubmit> getRecentChallengeSubmissions() {
        return recentChallengeSubmissions;
    }

    public void setRecentChallengeSubmissions(List<ChallengeSubmit> recentChallengeSubmissions) {
        this.recentChallengeSubmissions = recentChallengeSubmissions;
    }
}
