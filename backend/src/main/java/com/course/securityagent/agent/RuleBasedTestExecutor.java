package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RuleBasedTestExecutor implements TestExecutor {

    @Override
    public List<TestLog> execute(TestTask task) {
        List<TestLog> logs = new ArrayList<>();
        addLog(logs, task.getId(), "INFO", "Agent 已接收任务：" + task.getTaskName());
        addLog(logs, task.getId(), "INFO", "任务理解完成，目标地址：" + task.getTargetUrl());
        addLog(logs, task.getId(), "SUCCESS", "测试计划生成完成，规则模板：" + task.getTestType());
        addLog(logs, task.getId(), "INFO", "开始模拟执行低频安全测试用例");
        addLog(logs, task.getId(), "WARN", "检测到疑似风险特征，进入结果分析阶段");
        addLog(logs, task.getId(), "SUCCESS", "漏洞结果、修复建议与测试报告生成完成");
        return logs;
    }

    private void addLog(List<TestLog> logs, Long taskId, String level, String message) {
        TestLog log = new TestLog();
        log.setTaskId(taskId);
        log.setLogLevel(level);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        logs.add(log);
    }
}
