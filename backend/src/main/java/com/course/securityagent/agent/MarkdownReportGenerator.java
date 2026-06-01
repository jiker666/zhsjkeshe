package com.course.securityagent.agent;

import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MarkdownReportGenerator implements ReportGenerator {

    @Override
    public TestReport generate(TestTask task, List<TestResult> results, List<TestLog> logs) {
        TestReport report = new TestReport();
        report.setTaskId(task.getId());
        report.setTitle("课程设计演示用安全测试报告 - " + task.getTaskName());
        report.setFormat("MARKDOWN");
        report.setCreatedAt(LocalDateTime.now());

        Map<String, Long> riskCount = results.stream()
                .collect(Collectors.groupingBy(TestResult::getRiskLevel, Collectors.counting()));

        StringBuilder content = new StringBuilder();
        content.append("# ").append(report.getTitle()).append("\n\n");
        content.append("> 本报告由规则模拟版 AI Agent 自动生成，适用于课程设计演示、本地靶场或已授权目标的安全测试流程展示。\n\n");
        content.append("## 1. 报告基本信息\n\n");
        content.append("| 项目 | 内容 |\n| --- | --- |\n");
        content.append("| 生成时间 | ").append(report.getCreatedAt()).append(" |\n");
        content.append("| 报告格式 | Markdown |\n");
        content.append("| Agent 类型 | Rule-Based Security Agent |\n\n");

        content.append("## 2. 测试任务信息\n\n");
        content.append("| 项目 | 内容 |\n| --- | --- |\n");
        content.append("| 任务名称 | ").append(task.getTaskName()).append(" |\n");
        content.append("| 测试目标 | ").append(task.getTargetUrl()).append(" |\n");
        content.append("| 测试类型 | ").append(task.getTestType()).append(" |\n");
        content.append("| 测试深度 | ").append(task.getTestDepth()).append(" |\n");
        content.append("| 任务描述 | ").append(task.getDescription()).append(" |\n\n");

        content.append("## 3. 测试范围与方法\n\n");
        content.append("- 测试范围：仅限用户填写的目标地址及课程演示授权范围。\n");
        content.append("- 测试方法：规则模板生成测试计划，模拟请求构造、响应特征识别、风险等级评估和修复建议输出。\n");
        content.append("- 安全约束：不执行破坏性操作，不进行高频扫描，不绕过授权边界。\n\n");

        content.append("## 4. Agent 测试计划\n\n");
        content.append(task.getPlan()).append("\n");

        content.append("## 5. 风险统计\n\n");
        content.append("风险评分采用课程设计简化 CVSS-Lite 模型，综合风险等级、影响范围、可利用性和利用难度计算，分值范围 0.0 - 10.0。\n\n");
        content.append("| 风险等级 | 数量 |\n| --- | ---: |\n");
        content.append("| 高危 HIGH | ").append(riskCount.getOrDefault("HIGH", 0L)).append(" |\n");
        content.append("| 中危 MEDIUM | ").append(riskCount.getOrDefault("MEDIUM", 0L)).append(" |\n");
        content.append("| 低危 LOW | ").append(riskCount.getOrDefault("LOW", 0L)).append(" |\n\n");

        content.append("## 6. 漏洞详情\n\n");
        for (int i = 0; i < results.size(); i++) {
            TestResult result = results.get(i);
            content.append("### 6.").append(i + 1).append(" ").append(result.getVulnerabilityName()).append("\n\n");
            content.append("- 风险等级：").append(result.getRiskLevel()).append("\n");
            content.append("- 风险评分：").append(result.getRiskScore() == null ? "-" : result.getRiskScore()).append("\n");
            content.append("- 风险向量：").append(result.getRiskVector() == null ? "-" : result.getRiskVector()).append("\n");
            content.append("- 漏洞类型：").append(result.getVulnerabilityType()).append("\n");
            content.append("- 影响地址：").append(result.getUrl()).append("\n");
            content.append("- 漏洞描述：").append(result.getDescription()).append("\n");
            content.append("- 复现步骤：\n").append(result.getReproduceSteps()).append("\n\n");
            content.append("请求示例：\n\n```http\n").append(result.getRequestExample()).append("\n```\n\n");
            content.append("响应示例：\n\n```json\n").append(result.getResponseExample()).append("\n```\n\n");
            content.append("- 修复建议：").append(result.getSuggestion()).append("\n\n");
        }

        content.append("## 7. 测试日志摘要\n\n");
        logs.stream().limit(8).forEach(log -> content.append("- [")
                .append(log.getLogLevel()).append("] ")
                .append(log.getCreatedAt()).append(" ")
                .append(log.getMessage()).append("\n"));

        content.append("\n## 8. Agent 决策链摘要\n\n");
        content.append("- 任务理解：根据目标地址、测试类型和授权说明确定测试边界。\n");
        content.append("- 计划生成：按测试类型生成课程演示用测试计划。\n");
        content.append("- 工具选择：当前采用规则模拟 Agent，不调用真实攻击扫描器。\n");
        content.append("- 结果分析：将响应特征映射为风险等级、风险评分和修复建议。\n");
        content.append("- 报告生成：输出 Markdown/Word/HTML 形式的课程设计报告。\n\n");

        if (task.getTargetUrl() != null && task.getTargetUrl().contains("demo-target")) {
            content.append("## 9. 内置演示靶场说明\n\n");
            content.append("本次任务目标为系统内置 demo-target 模拟接口，仅用于课程设计演示，不代表真实攻击能力。\n\n");
        }

        content.append("## 10. 总结结论\n\n");
        content.append("本次测试完成了任务理解、计划生成、自动执行、结果分析和报告生成的闭环流程。")
                .append("当前结果为规则模拟 Agent 输出，可用于展示自主安全测试平台的软件工程设计与实现思路。")
                .append("后续可在授权范围内接入真实扫描器、大模型推理和更细粒度权限控制。\n");

        report.setContent(content.toString());
        return report;
    }
}
