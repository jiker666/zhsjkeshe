package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.service.RiskScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleBasedSecurityAgentServiceImpl implements SecurityAgentService {
    private final TestPlanGenerator testPlanGenerator;
    private final TestExecutor testExecutor;
    private final ResultAnalyzer resultAnalyzer;
    private final ReportGenerator reportGenerator;
    private final RiskScoreService riskScoreService;

    public RuleBasedSecurityAgentServiceImpl(TestPlanGenerator testPlanGenerator,
                                             TestExecutor testExecutor,
                                             ResultAnalyzer resultAnalyzer,
                                             ReportGenerator reportGenerator,
                                             RiskScoreService riskScoreService) {
        this.testPlanGenerator = testPlanGenerator;
        this.testExecutor = testExecutor;
        this.resultAnalyzer = resultAnalyzer;
        this.reportGenerator = reportGenerator;
        this.riskScoreService = riskScoreService;
    }

    @Override
    public String buildPlan(String targetUrl, String testType, String description) {
        return testPlanGenerator.generate(targetUrl, testType, description, "标准");
    }

    @Override
    public String buildPlan(String targetUrl, String testType, String description, String testDepth) {
        return testPlanGenerator.generate(targetUrl, testType, description, testDepth);
    }

    @Override
    public AgentExecutionResult execute(TestTask task) {
        List<TestLog> logs = testExecutor.execute(task);
        List<TestResult> results = resultAnalyzer.analyze(task);
        results.forEach(riskScoreService::applyScore);
        AgentExecutionResult executionResult = new AgentExecutionResult();
        executionResult.setLogs(logs);
        executionResult.setResults(results);
        executionResult.setReport(reportGenerator.generate(task, results, logs));
        return executionResult;
    }
}
