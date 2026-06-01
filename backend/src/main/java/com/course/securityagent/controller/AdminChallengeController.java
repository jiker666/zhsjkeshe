package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.PasswordUtil;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.ChallengeDTO;
import com.course.securityagent.dto.ChallengeSaveRequest;
import com.course.securityagent.entity.Challenge;
import com.course.securityagent.entity.ChallengeSubmit;
import com.course.securityagent.mapper.ChallengeMapper;
import com.course.securityagent.mapper.ChallengeSubmitMapper;
import com.course.securityagent.service.AuditLogService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/challenges")
public class AdminChallengeController {
    private final ChallengeMapper challengeMapper;
    private final ChallengeSubmitMapper submitMapper;
    private final AuditLogService auditLogService;

    public AdminChallengeController(ChallengeMapper challengeMapper,
                                    ChallengeSubmitMapper submitMapper,
                                    AuditLogService auditLogService) {
        this.challengeMapper = challengeMapper;
        this.submitMapper = submitMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<ChallengeDTO>> page(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String difficulty,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "1") Long page,
                                                      @RequestParam(defaultValue = "10") Long size) {
        requireAdmin();
        LambdaQueryWrapper<Challenge> wrapper = new LambdaQueryWrapper<Challenge>().orderByAsc(Challenge::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Challenge::getTitle, keyword).or().like(Challenge::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) wrapper.eq(Challenge::getCategory, category);
        if (StringUtils.hasText(difficulty)) wrapper.eq(Challenge::getDifficulty, difficulty);
        if (StringUtils.hasText(status)) wrapper.eq(Challenge::getStatus, status);
        Page<Challenge> result = challengeMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResult<>(
                result.getRecords().stream().map(this::adminDto).toList(),
                result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @PostMapping
    public ApiResponse<ChallengeDTO> create(@RequestBody ChallengeSaveRequest request) {
        requireAdmin();
        Challenge challenge = new Challenge();
        fill(challenge, request, true);
        challenge.setCreateUserId(UserContext.getUserId());
        challenge.setCreateTime(LocalDateTime.now());
        challenge.setUpdateTime(LocalDateTime.now());
        challengeMapper.insert(challenge);
        auditLogService.record("CREATE_CHALLENGE", "趣味靶场", "新增题目：" + challenge.getTitle(), "SUCCESS");
        return ApiResponse.ok(adminDto(challenge));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChallengeDTO> update(@PathVariable Long id, @RequestBody ChallengeSaveRequest request) {
        requireAdmin();
        Challenge challenge = challengeMapper.selectById(id);
        if (challenge == null) throw new IllegalArgumentException("题目不存在");
        fill(challenge, request, false);
        challenge.setUpdateTime(LocalDateTime.now());
        challengeMapper.updateById(challenge);
        auditLogService.record("EDIT_CHALLENGE", "趣味靶场", "编辑题目：" + challenge.getTitle(), "SUCCESS");
        return ApiResponse.ok(adminDto(challenge));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ChallengeDTO> status(@PathVariable Long id, @RequestParam String status) {
        requireAdmin();
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) {
            throw new IllegalArgumentException("不支持的题目状态");
        }
        Challenge challenge = challengeMapper.selectById(id);
        if (challenge == null) throw new IllegalArgumentException("题目不存在");
        challenge.setStatus(status);
        challenge.setUpdateTime(LocalDateTime.now());
        challengeMapper.updateById(challenge);
        auditLogService.record("UPDATE_CHALLENGE_STATUS", "趣味靶场", "题目状态更新：" + challenge.getTitle() + " -> " + status, "SUCCESS");
        return ApiResponse.ok(adminDto(challenge));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireAdmin();
        challengeMapper.deleteById(id);
        auditLogService.record("DELETE_CHALLENGE", "趣味靶场", "删除题目：" + id, "SUCCESS");
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/submissions")
    public ApiResponse<PageResult<ChallengeSubmit>> submissions(@PathVariable Long id,
                                                                @RequestParam(defaultValue = "1") Long page,
                                                                @RequestParam(defaultValue = "10") Long size) {
        requireAdmin();
        Page<ChallengeSubmit> result = submitMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, id)
                .orderByDesc(ChallengeSubmit::getSubmitTime));
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    private void fill(Challenge challenge, ChallengeSaveRequest request, boolean create) {
        if (!StringUtils.hasText(request.getTitle())) throw new IllegalArgumentException("题目标题不能为空");
        if (!StringUtils.hasText(request.getCategory())) throw new IllegalArgumentException("题目类型不能为空");
        if (!StringUtils.hasText(request.getDifficulty())) throw new IllegalArgumentException("题目难度不能为空");
        if (create && !StringUtils.hasText(request.getAnswer())) throw new IllegalArgumentException("题目答案不能为空");
        challenge.setTitle(request.getTitle());
        challenge.setCategory(request.getCategory());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setScore(request.getScore() == null ? 10 : request.getScore());
        challenge.setDescription(request.getDescription());
        challenge.setTargetUrl(request.getTargetUrl());
        challenge.setRequestMethod(StringUtils.hasText(request.getRequestMethod()) ? request.getRequestMethod() : "GET");
        challenge.setRequestExample(request.getRequestExample());
        challenge.setHint(request.getHint());
        if (StringUtils.hasText(request.getAnswer())) {
            challenge.setAnswerHash(PasswordUtil.hash(request.getAnswer().trim()));
        }
        challenge.setExplanation(request.getExplanation());
        challenge.setKnowledgeId(request.getKnowledgeId());
        challenge.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
    }

    private ChallengeDTO adminDto(Challenge challenge) {
        long submitCount = submitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, challenge.getId()));
        long correctCount = submitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, challenge.getId()).eq(ChallengeSubmit::getCorrect, 1));
        return ChallengeDTO.from(challenge, false, submitCount, correctCount, true);
    }

    private void requireAdmin() {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }
}
