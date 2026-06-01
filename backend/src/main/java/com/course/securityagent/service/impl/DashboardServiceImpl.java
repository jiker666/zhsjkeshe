package com.course.securityagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.DashboardStats;
import com.course.securityagent.entity.AgentDecision;
import com.course.securityagent.entity.AuditLog;
import com.course.securityagent.entity.Challenge;
import com.course.securityagent.entity.ChallengeSubmit;
import com.course.securityagent.entity.RemediationTicket;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.User;
import com.course.securityagent.entity.UserAchievement;
import com.course.securityagent.entity.UserScore;
import com.course.securityagent.mapper.AgentDecisionMapper;
import com.course.securityagent.mapper.AuditLogMapper;
import com.course.securityagent.mapper.ChallengeMapper;
import com.course.securityagent.mapper.ChallengeSubmitMapper;
import com.course.securityagent.mapper.RemediationTicketMapper;
import com.course.securityagent.mapper.TestReportMapper;
import com.course.securityagent.mapper.TestResultMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import com.course.securityagent.mapper.UserMapper;
import com.course.securityagent.mapper.UserAchievementMapper;
import com.course.securityagent.mapper.UserScoreMapper;
import com.course.securityagent.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final TestTaskMapper taskMapper;
    private final TestResultMapper resultMapper;
    private final RemediationTicketMapper ticketMapper;
    private final AuditLogMapper auditLogMapper;
    private final AgentDecisionMapper decisionMapper;
    private final TestReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ChallengeMapper challengeMapper;
    private final ChallengeSubmitMapper challengeSubmitMapper;
    private final UserScoreMapper userScoreMapper;
    private final UserAchievementMapper achievementMapper;

    public DashboardServiceImpl(TestTaskMapper taskMapper,
                                TestResultMapper resultMapper,
                                RemediationTicketMapper ticketMapper,
                                AuditLogMapper auditLogMapper,
                                AgentDecisionMapper decisionMapper,
                                TestReportMapper reportMapper,
                                UserMapper userMapper,
                                ChallengeMapper challengeMapper,
                                ChallengeSubmitMapper challengeSubmitMapper,
                                UserScoreMapper userScoreMapper,
                                UserAchievementMapper achievementMapper) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.ticketMapper = ticketMapper;
        this.auditLogMapper = auditLogMapper;
        this.decisionMapper = decisionMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.challengeMapper = challengeMapper;
        this.challengeSubmitMapper = challengeSubmitMapper;
        this.userScoreMapper = userScoreMapper;
        this.achievementMapper = achievementMapper;
    }

    @Override
    public DashboardStats stats() {
        DashboardStats stats = new DashboardStats();
        List<TestTask> tasks = taskMapper.selectList(taskScope());
        Set<Long> taskIds = tasks.stream().map(TestTask::getId).collect(Collectors.toSet());
        List<TestResult> results = taskIds.isEmpty()
                ? List.of()
                : resultMapper.selectList(new LambdaQueryWrapper<TestResult>().in(TestResult::getTaskId, taskIds));
        List<RemediationTicket> tickets = taskIds.isEmpty()
                ? List.of()
                : ticketMapper.selectList(new LambdaQueryWrapper<RemediationTicket>().in(RemediationTicket::getTaskId, taskIds));
        List<TestReport> reports = taskIds.isEmpty()
                ? List.of()
                : reportMapper.selectList(new LambdaQueryWrapper<TestReport>().in(TestReport::getTaskId, taskIds));

        stats.setTotalTasks((long) tasks.size());
        stats.setRunningTasks(tasks.stream().filter(task -> List.of("PLANNING", "RUNNING", "ANALYZING").contains(task.getStatus())).count());
        stats.setCompletedTasks(tasks.stream().filter(task -> "COMPLETED".equals(task.getStatus())).count());
        stats.setTotalVulnerabilities(results.size());
        stats.setHighRiskCount(results.stream().filter(result -> "HIGH".equals(result.getRiskLevel())).count());
        stats.setOpenVulnerabilities(results.stream().filter(result -> "OPEN".equals(result.getStatus())).count());
        stats.setTotalReports((long) reports.size());
        stats.setPendingTickets(tickets.stream().filter(ticket -> List.of("TODO", "CONFIRMED", "FIXING", "RETESTING").contains(ticket.getStatus())).count());
        stats.setClosedTickets(tickets.stream().filter(ticket -> "CLOSED".equals(ticket.getStatus())).count());
        stats.setTicketClosureRate(tickets.isEmpty() ? 0.0 : round(stats.getClosedTickets() * 100.0 / tickets.size()));
        stats.setAverageRiskScore(results.stream()
                .map(TestResult::getRiskScore)
                .filter(score -> score != null && score > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .map(this::round)
                .findFirst()
                .orElse(0.0));
        stats.setMaxRiskScore(results.stream()
                .map(TestResult::getRiskScore)
                .filter(score -> score != null && score > 0)
                .mapToDouble(Double::doubleValue)
                .max()
                .stream()
                .map(this::round)
                .findFirst()
                .orElse(0.0));
        LocalDate today = LocalDate.now();
        stats.setTodayTests(tasks.stream()
                .filter(task -> task.getCreatedAt() != null && task.getCreatedAt().toLocalDate().equals(today))
                .count());
        stats.setTotalUsers(UserContext.isAdmin() ? userMapper.selectCount(new LambdaQueryWrapper<User>()) : 0L);
        stats.setTodayAuditCount(UserContext.isAdmin()
                ? auditLogMapper.selectCount(new LambdaQueryWrapper<AuditLog>().ge(AuditLog::getCreateTime, today.atStartOfDay()))
                : 0L);
        stats.setAgentDecisionCount(taskIds.isEmpty()
                ? 0L
                : decisionMapper.selectCount(new LambdaQueryWrapper<AgentDecision>().in(AgentDecision::getTaskId, taskIds)));
        stats.setChallengeCount(challengeMapper.selectCount(new LambdaQueryWrapper<Challenge>().eq(Challenge::getStatus, "ENABLED")));
        long todaySubmits = challengeSubmitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .ge(ChallengeSubmit::getSubmitTime, today.atStartOfDay()));
        long correctSubmits = challengeSubmitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getCorrect, 1));
        long allSubmits = challengeSubmitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>());
        stats.setTodayChallengeSubmits(todaySubmits);
        stats.setChallengePassRate(allSubmits == 0 ? 0.0 : round(correctSubmits * 100.0 / allSubmits));
        UserScore myScore = userScoreMapper.selectOne(new LambdaQueryWrapper<UserScore>()
                .eq(UserScore::getUserId, UserContext.getUserId())
                .last("limit 1"));
        if (myScore != null) {
            stats.setMyChallengeScore(myScore.getTotalScore() == null ? 0 : myScore.getTotalScore());
            stats.setMyChallengeSolved(myScore.getSolvedCount() == null ? 0 : myScore.getSolvedCount());
        }
        stats.setMyAchievementCount(achievementMapper.selectCount(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, UserContext.getUserId())));

        Map<String, Long> riskDistribution = new LinkedHashMap<>();
        riskDistribution.put("HIGH", 0L);
        riskDistribution.put("MEDIUM", 0L);
        riskDistribution.put("LOW", 0L);
        riskDistribution.putAll(results.stream()
                .collect(Collectors.groupingBy(TestResult::getRiskLevel, Collectors.counting())));
        stats.setRiskDistribution(riskDistribution);

        Map<String, Long> taskTrend = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            taskTrend.put(date.toString(), tasks.stream()
                    .filter(task -> task.getCreatedAt() != null && task.getCreatedAt().toLocalDate().equals(date))
                    .count());
        }
        stats.setTaskTrend(taskTrend);

        Map<String, Long> testTypeDistribution = new LinkedHashMap<>();
        tasks.stream()
                .collect(Collectors.groupingBy(TestTask::getTestType, Collectors.counting()))
                .forEach(testTypeDistribution::put);
        stats.setTestTypeDistribution(testTypeDistribution);

        Map<String, Long> taskStatusDistribution = new LinkedHashMap<>();
        taskStatusDistribution.put("PENDING", 0L);
        taskStatusDistribution.put("PLANNING", 0L);
        taskStatusDistribution.put("RUNNING", 0L);
        taskStatusDistribution.put("ANALYZING", 0L);
        taskStatusDistribution.put("COMPLETED", 0L);
        taskStatusDistribution.put("FAILED", 0L);
        taskStatusDistribution.putAll(tasks.stream()
                .collect(Collectors.groupingBy(TestTask::getStatus, Collectors.counting())));
        stats.setTaskStatusDistribution(taskStatusDistribution);

        Map<String, Long> ticketStatusDistribution = new LinkedHashMap<>();
        ticketStatusDistribution.put("TODO", 0L);
        ticketStatusDistribution.put("CONFIRMED", 0L);
        ticketStatusDistribution.put("FIXING", 0L);
        ticketStatusDistribution.put("RETESTING", 0L);
        ticketStatusDistribution.put("CLOSED", 0L);
        ticketStatusDistribution.put("IGNORED", 0L);
        ticketStatusDistribution.putAll(tickets.stream()
                .collect(Collectors.groupingBy(RemediationTicket::getStatus, Collectors.counting())));
        stats.setTicketStatusDistribution(ticketStatusDistribution);

        stats.setRecentTasks(tasks.stream()
                .sorted((left, right) -> nullSafe(right.getCreatedAt()).compareTo(nullSafe(left.getCreatedAt())))
                .limit(5)
                .collect(Collectors.toList()));
        stats.setHighRiskTop(results.stream()
                .filter(result -> "HIGH".equals(result.getRiskLevel()))
                .sorted((left, right) -> nullSafe(right.getCreatedAt()).compareTo(nullSafe(left.getCreatedAt())))
                .limit(5)
                .collect(Collectors.toList()));
        stats.setRiskScoreTop(results.stream()
                .sorted((left, right) -> Double.compare(score(right), score(left)))
                .limit(5)
                .collect(Collectors.toList()));
        stats.setRecentTickets(tickets.stream()
                .sorted((left, right) -> nullSafe(right.getUpdateTime()).compareTo(nullSafe(left.getUpdateTime())))
                .limit(5)
                .collect(Collectors.toList()));
        stats.setRecentAuditLogs(UserContext.isAdmin()
                ? auditLogMapper.selectList(new LambdaQueryWrapper<AuditLog>()
                .orderByDesc(AuditLog::getCreateTime)
                .last("limit 6"))
                : List.of());
        stats.setScoreTop(UserContext.isAdmin()
                ? userScoreMapper.selectList(new LambdaQueryWrapper<UserScore>()
                .orderByDesc(UserScore::getTotalScore)
                .last("limit 5"))
                : List.of());
        LambdaQueryWrapper<ChallengeSubmit> submitWrapper = new LambdaQueryWrapper<ChallengeSubmit>()
                .orderByDesc(ChallengeSubmit::getSubmitTime)
                .last("limit 6");
        if (!UserContext.isAdmin()) {
            submitWrapper.eq(ChallengeSubmit::getUserId, UserContext.getUserId());
        }
        stats.setRecentChallengeSubmissions(challengeSubmitMapper.selectList(submitWrapper));
        return stats;
    }

    private LambdaQueryWrapper<TestTask> taskScope() {
        LambdaQueryWrapper<TestTask> wrapper = new LambdaQueryWrapper<TestTask>();
        if (!UserContext.isAdmin()) {
            wrapper.eq(TestTask::getCreatedBy, UserContext.getUserId());
        }
        return wrapper;
    }

    private double score(TestResult result) {
        return result.getRiskScore() == null ? 0.0 : result.getRiskScore();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private java.time.LocalDateTime nullSafe(java.time.LocalDateTime value) {
        return value == null ? java.time.LocalDateTime.MIN : value;
    }
}
