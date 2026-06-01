package com.course.securityagent.service;

import com.course.securityagent.entity.TestResult;
import org.springframework.stereotype.Service;

@Service
public class RiskScoreService {

    public void applyScore(TestResult result) {
        double score = scoreFor(result);
        result.setRiskScore(score);
        result.setImpactScore(impactFor(score));
        result.setExploitabilityScore(exploitabilityFor(result, score));
        result.setDifficultyScore(difficultyFor(result, score));
        result.setRiskVector(vectorFor(result, score));
    }

    private double scoreFor(TestResult result) {
        String name = value(result.getVulnerabilityName());
        String type = value(result.getVulnerabilityType());
        if (name.contains("未授权")) {
            return 9.1;
        }
        if (name.contains("水平越权") || name.contains("越权")) {
            return name.contains("订单") ? 8.6 : 7.4;
        }
        if (name.contains("弱口令")) {
            return 6.5;
        }
        if (name.contains("敏感字段") || type.contains("信息泄露")) {
            return "LOW".equals(result.getRiskLevel()) ? 3.2 : 5.8;
        }
        if (name.contains("参数") || type.contains("业务安全")) {
            return 6.2;
        }
        if ("HIGH".equals(result.getRiskLevel())) {
            return 8.2;
        }
        if ("MEDIUM".equals(result.getRiskLevel())) {
            return 5.4;
        }
        return 2.4;
    }

    private double impactFor(double score) {
        return Math.min(10.0, Math.round((score + 0.6) * 10.0) / 10.0);
    }

    private double exploitabilityFor(TestResult result, double score) {
        String text = value(result.getDescription()) + value(result.getReproduceSteps());
        double base = score >= 7 ? 8.4 : score >= 4 ? 6.1 : 3.6;
        if (text.contains("未登录") || text.contains("修改") || text.contains("常见弱密码")) {
            base += 0.5;
        }
        return Math.min(10.0, Math.round(base * 10.0) / 10.0);
    }

    private double difficultyFor(TestResult result, double score) {
        String text = value(result.getReproduceSteps());
        double base = score >= 7 ? 2.4 : score >= 4 ? 4.2 : 6.5;
        if (text.contains("清空登录态") || text.contains("修改")) {
            base -= 0.5;
        }
        return Math.max(1.0, Math.round(base * 10.0) / 10.0);
    }

    private String vectorFor(TestResult result, double score) {
        String priority = score >= 7 ? "P1/优先修复" : score >= 4 ? "P2/计划修复" : "P3/例行优化";
        return "SCORE:" + score + ";LEVEL:" + result.getRiskLevel() + ";PRIORITY:" + priority
                + ";MODEL:DEMO-CVSS-LITE";
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
