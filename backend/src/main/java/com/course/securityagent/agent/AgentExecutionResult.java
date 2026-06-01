package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;

import java.util.List;

public class AgentExecutionResult {
    private List<TestLog> logs;
    private List<TestResult> results;
    private TestReport report;

    public List<TestLog> getLogs() {
        return logs;
    }

    public void setLogs(List<TestLog> logs) {
        this.logs = logs;
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void setResults(List<TestResult> results) {
        this.results = results;
    }

    public TestReport getReport() {
        return report;
    }

    public void setReport(TestReport report) {
        this.report = report;
    }
}
