package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RuleBasedSecurityAgent {

    public String buildPlan(String targetUrl, String testType, String description) {
        return """
                1. 解析目标地址与测试类型，确认测试边界。
                2. 根据测试类型选择规则模板，生成测试用例。
                3. 模拟构造安全测试请求并记录关键步骤。
                4. 根据规则判断潜在风险并生成漏洞结果。
                5. 汇总测试日志、风险等级和修复建议，形成报告。
                """.formatted();
    }

    public AgentExecutionResult execute(TestTask task) {
        AgentExecutionResult executionResult = new AgentExecutionResult();
        executionResult.setLogs(buildLogs(task));
        executionResult.setResults(buildResults(task));
        executionResult.setReport(buildReport(task, executionResult.getResults()));
        return executionResult;
    }

    private List<TestLog> buildLogs(TestTask task) {
        List<TestLog> logs = new ArrayList<>();
        addLog(logs, task.getId(), "INFO", "Agent 接收任务：" + task.getTaskName());
        addLog(logs, task.getId(), "INFO", "目标地址校验完成：" + task.getTargetUrl());
        addLog(logs, task.getId(), "INFO", "加载规则模板：" + task.getTestType());
        addLog(logs, task.getId(), "INFO", "模拟执行测试用例并收集响应特征");
        addLog(logs, task.getId(), "WARN", "发现疑似风险点，已生成漏洞结果和修复建议");
        addLog(logs, task.getId(), "INFO", "任务执行完成，报告已生成");
        return logs;
    }

    private List<TestResult> buildResults(TestTask task) {
        return switch (task.getTestType()) {
            case "未授权访问测试" -> List.of(
                    result(task, "接口未授权访问风险", "HIGH", "/api/admin/users",
                            "敏感接口在未携带有效登录态时仍可能返回业务数据。",
                            "为敏感接口统一接入认证拦截器，并在服务端校验用户身份。"),
                    result(task, "静态资源目录访问控制不足", "LOW", "/uploads/",
                            "部分资源目录缺少访问控制策略，可能暴露内部文件索引。",
                            "关闭目录索引，对私有资源增加鉴权和临时访问链接。")
            );
            case "弱口令测试" -> List.of(
                    result(task, "默认口令风险", "HIGH", "/login",
                            "系统存在疑似 admin/123456 等弱口令组合风险。",
                            "启用强密码策略、登录失败锁定和初始密码强制修改。"),
                    result(task, "登录错误提示过于明确", "LOW", "/login",
                            "登录失败提示可能辅助攻击者判断账号是否存在。",
                            "统一登录失败提示，避免暴露账号枚举信息。")
            );
            case "越权访问测试" -> List.of(
                    result(task, "水平越权访问风险", "HIGH", "/api/orders/{id}",
                            "通过修改资源 ID 可能访问其他用户的数据。",
                            "在查询资源时同时校验资源归属用户或租户。"),
                    result(task, "角色权限校验不完整", "MEDIUM", "/api/admin/config",
                            "部分管理接口只校验登录状态，缺少角色级权限判断。",
                            "建立 RBAC 权限模型，并对管理操作添加角色/权限校验。")
            );
            case "敏感信息泄露测试" -> List.of(
                    result(task, "响应中包含敏感字段", "MEDIUM", "/api/profile",
                            "接口响应中包含手机号、邮箱等敏感字段且未做脱敏。",
                            "按最小必要原则返回字段，对敏感数据进行脱敏处理。"),
                    result(task, "调试信息泄露", "LOW", "/error",
                            "异常页面可能返回堆栈或框架版本信息。",
                            "关闭生产环境详细错误输出，统一异常响应格式。")
            );
            case "接口参数篡改测试" -> List.of(
                    result(task, "参数篡改导致业务风险", "MEDIUM", "/api/payments",
                            "金额、状态等关键参数缺少服务端二次校验。",
                            "关键业务参数应由服务端计算或从可信数据源读取。"),
                    result(task, "缺少参数白名单校验", "LOW", "/api/search",
                            "接口接受未声明参数，可能造成非预期查询行为。",
                            "增加参数白名单和类型校验，拒绝非法参数。")
            );
            default -> List.of(
                    result(task, "通用安全配置风险", "LOW", task.getTargetUrl(),
                            "当前测试类型未匹配专用规则，系统生成通用风险提示。",
                            "补充更细粒度规则模板，并接入真实扫描器。")
            );
        };
    }

    private TestReport buildReport(TestTask task, List<TestResult> results) {
        TestReport report = new TestReport();
        report.setTaskId(task.getId());
        report.setTitle("安全测试报告 - " + task.getTaskName());
        report.setFormat("MARKDOWN");
        report.setCreatedAt(LocalDateTime.now());

        StringBuilder content = new StringBuilder();
        content.append("# ").append(report.getTitle()).append("\n\n");
        content.append("## 一、任务概况\n");
        content.append("- 目标地址：").append(task.getTargetUrl()).append("\n");
        content.append("- 测试类型：").append(task.getTestType()).append("\n");
        content.append("- 任务描述：").append(task.getDescription()).append("\n\n");
        content.append("## 二、测试计划\n").append(task.getPlan()).append("\n");
        content.append("## 三、漏洞结果\n");
        for (TestResult result : results) {
            content.append("- 【").append(result.getRiskLevel()).append("】")
                    .append(result.getVulnerabilityName()).append("：")
                    .append(result.getDescription()).append("\n")
                    .append("  - 修复建议：").append(result.getSuggestion()).append("\n");
        }
        content.append("\n## 四、结论\n");
        content.append("本报告由规则模拟版 Agent 自动生成，用于课程设计演示。后续可替换为真实 AI Agent 与扫描器执行结果。\n");
        report.setContent(content.toString());
        return report;
    }

    private void addLog(List<TestLog> logs, Long taskId, String level, String message) {
        TestLog log = new TestLog();
        log.setTaskId(taskId);
        log.setLogLevel(level);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        logs.add(log);
    }

    private TestResult result(TestTask task, String name, String level, String path, String description, String suggestion) {
        TestResult result = new TestResult();
        result.setTaskId(task.getId());
        result.setVulnerabilityName(name);
        result.setRiskLevel(level);
        result.setUrl(task.getTargetUrl() + path);
        result.setDescription(description);
        result.setSuggestion(suggestion);
        result.setStatus("OPEN");
        result.setCreatedAt(LocalDateTime.now());
        return result;
    }
}
