package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;

import java.util.List;

public interface ReportGenerator {
    TestReport generate(TestTask task, List<TestResult> results, List<TestLog> logs);
}
