package com.course.securityagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.agent.AgentExecutionResult;
import com.course.securityagent.agent.SecurityAgentService;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.TaskCreateRequest;
import com.course.securityagent.dto.TaskUpdateRequest;
import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.User;
import com.course.securityagent.entity.AgentDecision;
import com.course.securityagent.entity.RemediationTicket;
import com.course.securityagent.mapper.AgentDecisionMapper;
import com.course.securityagent.mapper.RemediationTicketMapper;
import com.course.securityagent.mapper.TestLogMapper;
import com.course.securityagent.mapper.TestReportMapper;
import com.course.securityagent.mapper.TestResultMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import com.course.securityagent.mapper.UserMapper;
import com.course.securityagent.service.AuditLogService;
import com.course.securityagent.service.TestTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TestTaskServiceImpl implements TestTaskService {
    private final TestTaskMapper taskMapper;
    private final TestResultMapper resultMapper;
    private final TestLogMapper logMapper;
    private final TestReportMapper reportMapper;
    private final UserMapper userMapper;
    private final SecurityAgentService agent;
    private final AgentDecisionMapper decisionMapper;
    private final RemediationTicketMapper ticketMapper;
    private final AuditLogService auditLogService;

    public TestTaskServiceImpl(TestTaskMapper taskMapper,
                               TestResultMapper resultMapper,
                               TestLogMapper logMapper,
                               TestReportMapper reportMapper,
                               UserMapper userMapper,
                               SecurityAgentService agent,
                               AgentDecisionMapper decisionMapper,
                               RemediationTicketMapper ticketMapper,
                               AuditLogService auditLogService) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.logMapper = logMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.agent = agent;
        this.decisionMapper = decisionMapper;
        this.ticketMapper = ticketMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<TestTask> listTasks(String keyword, String status) {
        LambdaQueryWrapper<TestTask> wrapper = new LambdaQueryWrapper<TestTask>()
                .orderByDesc(TestTask::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TestTask::getTaskName, keyword)
                    .or()
                    .like(TestTask::getTargetUrl, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TestTask::getStatus, status);
        }
        applyOwnerScope(wrapper);
        return taskMapper.selectList(wrapper);
    }

    @Override
    public PageResult<TestTask> pageTasks(String keyword, String testType, String status, String riskLevel, long page, long size) {
        LambdaQueryWrapper<TestTask> wrapper = new LambdaQueryWrapper<TestTask>()
                .orderByDesc(TestTask::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TestTask::getTaskName, keyword)
                    .or()
                    .like(TestTask::getTargetUrl, keyword));
        }
        if (StringUtils.hasText(testType)) {
            wrapper.eq(TestTask::getTestType, testType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TestTask::getStatus, status);
        }
        if (StringUtils.hasText(riskLevel)) {
            List<Long> taskIds = resultMapper.selectList(new LambdaQueryWrapper<TestResult>()
                            .eq(TestResult::getRiskLevel, riskLevel))
                    .stream()
                    .map(TestResult::getTaskId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (taskIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, page, size);
            }
            wrapper.in(TestTask::getId, taskIds);
        }
        applyOwnerScope(wrapper);
        Page<TestTask> result = taskMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public TestTask getTask(Long id) {
        TestTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("测试任务不存在");
        }
        ensureTaskOwner(task);
        return task;
    }

    @Override
    public TestTask createTask(TaskCreateRequest request) {
        TestTask task = new TestTask();
        task.setTaskName(request.getTaskName());
        task.setTargetUrl(request.getTargetUrl());
        task.setTestType(request.getTestType());
        task.setDescription(request.getDescription());
        task.setTestDepth(StringUtils.hasText(request.getTestDepth()) ? request.getTestDepth() : "标准");
        task.setGenerateReport(request.getGenerateReport() == null ? 1 : request.getGenerateReport());
        task.setPlan(agent.buildPlan(request.getTargetUrl(), request.getTestType(), request.getDescription(), task.getTestDepth()));
        task.setStatus("PENDING");
        task.setCreatedBy(UserContext.getUserId());
        task.setCreateUsername(currentUsername());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        auditLogService.record("CREATE_TASK", "测试任务", "创建任务：" + task.getTaskName(), "SUCCESS");
        return task;
    }

    @Override
    public TestTask updateTask(Long id, TaskUpdateRequest request) {
        TestTask task = getTask(id);
        if (StringUtils.hasText(request.getTaskName())) {
            task.setTaskName(request.getTaskName());
        }
        if (StringUtils.hasText(request.getTargetUrl())) {
            task.setTargetUrl(request.getTargetUrl());
        }
        if (StringUtils.hasText(request.getTestType())) {
            task.setTestType(request.getTestType());
        }
        task.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getTestDepth())) {
            task.setTestDepth(request.getTestDepth());
        }
        if (request.getGenerateReport() != null) {
            task.setGenerateReport(request.getGenerateReport());
        }
        if (StringUtils.hasText(request.getStatus())) {
            task.setStatus(request.getStatus());
        }
        task.setPlan(agent.buildPlan(task.getTargetUrl(), task.getTestType(), task.getDescription(), task.getTestDepth()));
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        auditLogService.record("EDIT_TASK", "测试任务", "编辑任务：" + task.getTaskName(), "SUCCESS");
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        TestTask task = getTask(id);
        taskMapper.deleteById(id);
        resultMapper.delete(new LambdaQueryWrapper<TestResult>().eq(TestResult::getTaskId, id));
        logMapper.delete(new LambdaQueryWrapper<TestLog>().eq(TestLog::getTaskId, id));
        reportMapper.delete(new LambdaQueryWrapper<TestReport>().eq(TestReport::getTaskId, id));
        decisionMapper.delete(new LambdaQueryWrapper<AgentDecision>().eq(AgentDecision::getTaskId, id));
        ticketMapper.delete(new LambdaQueryWrapper<RemediationTicket>().eq(RemediationTicket::getTaskId, id));
        auditLogService.record("DELETE_TASK", "测试任务", "删除任务：" + task.getTaskName(), "SUCCESS");
    }

    @Override
    @Transactional
    public TestTask executeTask(Long id) {
        TestTask task = getTask(id);
        resultMapper.delete(new LambdaQueryWrapper<TestResult>().eq(TestResult::getTaskId, id));
        logMapper.delete(new LambdaQueryWrapper<TestLog>().eq(TestLog::getTaskId, id));
        reportMapper.delete(new LambdaQueryWrapper<TestReport>().eq(TestReport::getTaskId, id));
        decisionMapper.delete(new LambdaQueryWrapper<AgentDecision>().eq(AgentDecision::getTaskId, id));

        updateStatus(task, "PLANNING", "INFO", "进入计划生成阶段，Agent 正在理解任务目标与测试类型");
        task.setPlan(agent.buildPlan(task.getTargetUrl(), task.getTestType(), task.getDescription(), task.getTestDepth()));
        taskMapper.updateById(task);

        updateStatus(task, "RUNNING", "INFO", "进入自动执行阶段，开始模拟安全测试用例");
        AgentExecutionResult executionResult = agent.execute(task);
        executionResult.getLogs().forEach(logMapper::insert);

        updateStatus(task, "ANALYZING", "WARN", "进入结果分析阶段，正在评估风险等级与修复建议");
        executionResult.getResults().forEach(resultMapper::insert);
        generateDecisionChain(task, executionResult);
        if (task.getGenerateReport() == null || task.getGenerateReport() == 1) {
            reportMapper.insert(executionResult.getReport());
            auditLogService.record("GENERATE_REPORT", "报告中心", "生成任务报告：" + task.getTaskName(), "SUCCESS");
        }

        updateStatus(task, "COMPLETED", "SUCCESS", "任务执行完成，漏洞结果与测试报告已生成");
        auditLogService.record("EXECUTE_TASK", "Agent执行", "执行任务：" + task.getTaskName(), "SUCCESS");
        return task;
    }

    private void applyOwnerScope(LambdaQueryWrapper<TestTask> wrapper) {
        if (!UserContext.isAdmin()) {
            wrapper.eq(TestTask::getCreatedBy, UserContext.getUserId());
        }
    }

    private void ensureTaskOwner(TestTask task) {
        if (!UserContext.isAdmin() && !Objects.equals(task.getCreatedBy(), UserContext.getUserId())) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }

    private String currentUsername() {
        User user = userMapper.selectById(UserContext.getUserId());
        return user == null ? "unknown" : user.getUsername();
    }

    private void updateStatus(TestTask task, String status, String level, String message) {
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        TestLog log = new TestLog();
        log.setTaskId(task.getId());
        log.setLogLevel(level);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    private void generateDecisionChain(TestTask task, AgentExecutionResult executionResult) {
        int resultCount = executionResult.getResults() == null ? 0 : executionResult.getResults().size();
        double maxScore = executionResult.getResults() == null ? 0.0 : executionResult.getResults().stream()
                .map(TestResult::getRiskScore)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(0.0);
        insertDecision(task, 1, "TASK_UNDERSTANDING", "识别测试任务意图",
                "Agent 根据任务目标、测试类型和描述判断本次测试边界，确认仅用于本地靶场或授权环境。",
                "目标：" + task.getTargetUrl() + "；类型：" + task.getTestType(),
                "确认测试类型为“" + task.getTestType() + "”，测试深度为“" + task.getTestDepth() + "”。",
                "TaskInterpreter", 0.94);
        insertDecision(task, 2, "PLAN_GENERATION", "生成分阶段测试计划",
                "根据规则模板将测试过程拆分为范围确认、用例构造、模拟执行、风险分析和报告输出。",
                "任务描述：" + safe(task.getDescription()),
                "已生成 " + countPlanSteps(task.getPlan()) + " 个计划步骤。",
                "RuleBasedPlanGenerator", 0.91);
        insertDecision(task, 3, "TOOL_SELECTION", "选择安全的规则模拟工具",
                "课程设计阶段不接入真实攻击扫描器，选择规则模拟执行器保证演示稳定和安全合规。",
                "目标环境：" + task.getTargetUrl(),
                "选择 RuleBasedTestExecutor、RuleBasedResultAnalyzer 和 CVSS-Lite 风险评分模型。",
                "ToolSelector", 0.88);
        insertDecision(task, 4, "TEST_EXECUTION", "执行授权范围内的模拟测试",
                "按测试类型构造低风险演示请求与响应样例，不对外部目标发起破坏性扫描。",
                "测试类型：" + task.getTestType(),
                "生成执行日志 " + (executionResult.getLogs() == null ? 0 : executionResult.getLogs().size()) + " 条。",
                "RuleBasedTestExecutor", 0.9);
        insertDecision(task, 5, "RESULT_ANALYSIS", "分析漏洞风险与修复优先级",
                "结合漏洞类型、风险等级、影响范围和可利用性，输出风险评分及修复建议。",
                "模拟响应特征与规则命中结果",
                "发现 " + resultCount + " 条漏洞，最高风险评分 " + maxScore + "。",
                "RiskScoreService", 0.87);
        insertDecision(task, 6, "REPORT_GENERATION", "生成正式测试报告",
                "将任务信息、决策链摘要、漏洞详情、风险评分、日志摘要和修复建议组织成报告。",
                "漏洞结果与日志摘要",
                "生成 Markdown 报告，可继续导出 HTML、Word 或浏览器打印 PDF。",
                "MarkdownReportGenerator", 0.93);
    }

    private void insertDecision(TestTask task, int stepNo, String stage, String title, String reason,
                                String input, String output, String toolName, double confidence) {
        AgentDecision decision = new AgentDecision();
        decision.setTaskId(task.getId());
        decision.setStepNo(stepNo);
        decision.setStage(stage);
        decision.setDecisionTitle(title);
        decision.setDecisionReason(reason);
        decision.setInputSummary(input);
        decision.setOutputSummary(output);
        decision.setToolName(toolName);
        decision.setConfidence(confidence);
        decision.setCreateTime(LocalDateTime.now());
        decisionMapper.insert(decision);
    }

    private int countPlanSteps(String plan) {
        if (!StringUtils.hasText(plan)) {
            return 0;
        }
        return (int) plan.lines().filter(line -> line.matches("\\d+\\..*")).count();
    }

    private String safe(String text) {
        return StringUtils.hasText(text) ? text : "无";
    }
}
