package com.course.securityagent.agent;

import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;

import java.util.List;

public interface ResultAnalyzer {
    List<TestResult> analyze(TestTask task);
}
