package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestTask;

import java.util.List;

public interface TestExecutor {
    List<TestLog> execute(TestTask task);
}
