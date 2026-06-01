package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.TestLog;
import com.course.securityagent.entity.TestReport;
import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import com.course.securityagent.entity.AgentDecision;
import com.course.securityagent.entity.RemediationTicket;
import com.course.securityagent.entity.SecurityKnowledge;
import com.course.securityagent.mapper.AgentDecisionMapper;
import com.course.securityagent.mapper.RemediationTicketMapper;
import com.course.securityagent.mapper.SecurityKnowledgeMapper;
import com.course.securityagent.mapper.TestLogMapper;
import com.course.securityagent.mapper.TestReportMapper;
import com.course.securityagent.mapper.TestResultMapper;
import com.course.securityagent.mapper.TestTaskMapper;
import com.course.securityagent.service.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class TestReportController {
    private final TestReportMapper reportMapper;
    private final TestTaskMapper taskMapper;
    private final TestResultMapper resultMapper;
    private final TestLogMapper logMapper;
    private final AgentDecisionMapper decisionMapper;
    private final RemediationTicketMapper ticketMapper;
    private final SecurityKnowledgeMapper knowledgeMapper;
    private final AuditLogService auditLogService;

    public TestReportController(TestReportMapper reportMapper,
                                TestTaskMapper taskMapper,
                                TestResultMapper resultMapper,
                                TestLogMapper logMapper,
                                AgentDecisionMapper decisionMapper,
                                RemediationTicketMapper ticketMapper,
                                SecurityKnowledgeMapper knowledgeMapper,
                                AuditLogService auditLogService) {
        this.reportMapper = reportMapper;
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.logMapper = logMapper;
        this.decisionMapper = decisionMapper;
        this.ticketMapper = ticketMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<TestReport> getByTaskId(@PathVariable Long taskId) {
        ensureTaskOwner(taskId);
        TestReport report = reportMapper.selectOne(new LambdaQueryWrapper<TestReport>()
                .eq(TestReport::getTaskId, taskId)
                .orderByDesc(TestReport::getCreatedAt)
                .last("limit 1"));
        return ApiResponse.ok(report);
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TestReport>> page(@RequestParam(defaultValue = "1") Long page,
                                                    @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<TestReport> wrapper = new LambdaQueryWrapper<TestReport>()
                .orderByDesc(TestReport::getCreatedAt);
        applyOwnerScope(wrapper);
        Page<TestReport> result = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}/export/docx")
    public void exportDocx(@PathVariable Long id, HttpServletResponse response) throws Exception {
        TestReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }
        ensureTaskOwner(report.getTaskId());
        TestTask task = taskMapper.selectById(report.getTaskId());
        List<TestResult> results = resultMapper.selectList(new LambdaQueryWrapper<TestResult>()
                .eq(TestResult::getTaskId, report.getTaskId())
                .orderByAsc(TestResult::getId));
        List<TestLog> logs = logMapper.selectList(new LambdaQueryWrapper<TestLog>()
                .eq(TestLog::getTaskId, report.getTaskId())
                .orderByAsc(TestLog::getCreatedAt)
                .last("limit 10"));
        List<AgentDecision> decisions = decisionMapper.selectList(new LambdaQueryWrapper<AgentDecision>()
                .eq(AgentDecision::getTaskId, report.getTaskId())
                .orderByAsc(AgentDecision::getStepNo));
        List<RemediationTicket> tickets = ticketMapper.selectList(new LambdaQueryWrapper<RemediationTicket>()
                .eq(RemediationTicket::getTaskId, report.getTaskId())
                .orderByAsc(RemediationTicket::getId));

        try (XWPFDocument document = new XWPFDocument()) {
            addTitle(document, report.getTitle());
            addParagraph(document, "报告类型：课程设计演示用安全测试报告");
            addParagraph(document, "生成日期：" + LocalDate.now());
            addHeading(document, "一、测试任务信息");
            addParagraph(document, "任务名称：" + value(task == null ? null : task.getTaskName()));
            addParagraph(document, "测试目标：" + value(task == null ? null : task.getTargetUrl()));
            addParagraph(document, "测试类型：" + value(task == null ? null : task.getTestType()));
            addParagraph(document, "测试深度：" + value(task == null ? null : task.getTestDepth()));
            addParagraph(document, "测试范围：本地靶场、课程演示环境或已授权目标。");
            addHeading(document, "二、风险统计");
            addParagraph(document, "高危：" + count(results, "HIGH") + "，中危：" + count(results, "MEDIUM") + "，低危：" + count(results, "LOW"));
            addParagraph(document, "平均风险评分：" + avgScore(results) + "，最高风险评分：" + maxScore(results));
            addParagraph(document, "评分说明：本系统采用课程设计简化 CVSS-Lite 模型，综合风险等级、影响范围、可利用性和利用难度生成 0.0 - 10.0 分。");
            addHeading(document, "三、漏洞详情");
            for (TestResult result : results) {
                addHeading(document, result.getVulnerabilityName() + "（" + result.getRiskLevel() + "）");
                addParagraph(document, "风险评分：" + value(result.getRiskScore() == null ? null : String.valueOf(result.getRiskScore())));
                addParagraph(document, "风险向量：" + value(result.getRiskVector()));
                addParagraph(document, "漏洞类型：" + value(result.getVulnerabilityType()));
                addParagraph(document, "影响地址：" + value(result.getUrl()));
                addParagraph(document, "漏洞描述：" + value(result.getDescription()));
                addParagraph(document, "复现步骤：" + value(result.getReproduceSteps()));
                addParagraph(document, "请求示例：" + value(result.getRequestExample()));
                addParagraph(document, "响应示例：" + value(result.getResponseExample()));
                addParagraph(document, "修复建议：" + value(result.getSuggestion()));
                SecurityKnowledge knowledge = knowledgeMapper.selectOne(new LambdaQueryWrapper<SecurityKnowledge>()
                        .like(SecurityKnowledge::getVulnType, result.getVulnerabilityType())
                        .last("limit 1"));
                if (knowledge != null) {
                    addParagraph(document, "知识库引用：" + knowledge.getTitle() + " - " + knowledge.getFixAdvice());
                }
            }
            addHeading(document, "四、Agent 决策链摘要");
            for (AgentDecision decision : decisions) {
                addParagraph(document, decision.getStepNo() + ". " + decision.getDecisionTitle()
                        + "（" + decision.getToolName() + "，置信度 " + decision.getConfidence() + "）：" + decision.getDecisionReason());
            }
            addHeading(document, "五、整改工单状态");
            if (tickets.isEmpty()) {
                addParagraph(document, "当前任务尚未生成整改工单。");
            }
            for (RemediationTicket ticket : tickets) {
                addParagraph(document, ticket.getTitle() + "，状态：" + ticket.getStatus() + "，负责人：" + value(ticket.getAssigneeName()) + "，复测：" + value(ticket.getRetestResult()));
            }
            addHeading(document, "六、测试日志摘要");
            for (TestLog log : logs) {
                addParagraph(document, "[" + log.getLogLevel() + "] " + log.getCreatedAt() + " " + log.getMessage());
            }
            if (task != null && task.getTargetUrl() != null && task.getTargetUrl().contains("demo-target")) {
                addHeading(document, "七、内置演示靶场说明");
                addParagraph(document, "本任务使用系统内置 demo-target 模拟接口，仅用于课程设计演示，不依赖外部系统，不代表真实攻击功能。");
            }
            addHeading(document, "八、测试结论");
            addParagraph(document, "本报告由规则模拟版 AI Agent 自动生成，用于展示安全测试任务编排、模拟执行、结果分析和报告输出闭环。后续可在授权范围内接入真实 AI Agent 与扫描器。");

            String taskName = task == null ? "未知任务" : task.getTaskName();
            String fileName = "安全测试报告_" + taskName + "_" + LocalDate.now() + ".docx";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            document.write(response.getOutputStream());
            auditLogService.record("EXPORT_REPORT", "报告中心", "导出 Word 报告：" + report.getTitle(), "SUCCESS");
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
        reportMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void applyOwnerScope(LambdaQueryWrapper<TestReport> wrapper) {
        if (UserContext.isAdmin()) {
            return;
        }
        List<Long> taskIds = taskMapper.selectList(new LambdaQueryWrapper<TestTask>()
                        .eq(TestTask::getCreatedBy, UserContext.getUserId()))
                .stream()
                .map(TestTask::getId)
                .toList();
        if (taskIds.isEmpty()) {
            wrapper.eq(TestReport::getTaskId, -1L);
            return;
        }
        wrapper.in(TestReport::getTaskId, taskIds);
    }

    private void ensureTaskOwner(Long taskId) {
        if (UserContext.isAdmin()) {
            return;
        }
        TestTask task = taskMapper.selectById(taskId);
        if (task == null || !UserContext.getUserId().equals(task.getCreatedBy())) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }

    private void addTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(20);
        run.setText(value(text));
    }

    private void addHeading(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(14);
        run.setText(value(text));
    }

    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(11);
        String[] lines = value(text).split("\\n");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                run.addBreak();
            }
            run.setText(lines[i]);
        }
    }

    private long count(List<TestResult> results, String level) {
        return results.stream().filter(result -> level.equals(result.getRiskLevel())).count();
    }

    private double avgScore(List<TestResult> results) {
        return Math.round(results.stream()
                .map(TestResult::getRiskScore)
                .filter(score -> score != null && score > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * 10.0) / 10.0;
    }

    private double maxScore(List<TestResult> results) {
        return Math.round(results.stream()
                .map(TestResult::getRiskScore)
                .filter(score -> score != null && score > 0)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0) * 10.0) / 10.0;
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}
