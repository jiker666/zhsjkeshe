package com.course.securityagent.agent;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RuleBasedTestPlanGenerator implements TestPlanGenerator {

    @Override
    public String generate(String targetUrl, String testType, String description, String testDepth) {
        String depth = StringUtils.hasText(testDepth) ? testDepth : "标准";
        return """
                1. 任务理解：确认目标地址 %s，测试类型为 %s，测试深度为 %s。
                2. 范围约束：仅针对课程演示、本地靶场或已授权目标进行低频模拟验证。
                3. 用例生成：根据测试类型选择规则模板，生成认证、参数、响应特征等检查点。
                4. 执行策略：模拟构造请求、观察状态码与响应字段，不执行破坏性操作。
                5. 结果分析：对疑似风险进行等级评估，形成漏洞描述与修复建议。
                6. 报告输出：汇总任务信息、风险统计、漏洞详情、日志摘要和结论。
                """.formatted(targetUrl, testType, depth);
    }
}
