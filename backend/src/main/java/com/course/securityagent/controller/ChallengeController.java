package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.PasswordUtil;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.dto.ChallengeDTO;
import com.course.securityagent.dto.ChallengeSubmitRequest;
import com.course.securityagent.dto.ChallengeSubmitResponse;
import com.course.securityagent.entity.Challenge;
import com.course.securityagent.entity.ChallengeSubmit;
import com.course.securityagent.entity.SecurityKnowledge;
import com.course.securityagent.entity.User;
import com.course.securityagent.entity.UserAchievement;
import com.course.securityagent.entity.UserScore;
import com.course.securityagent.mapper.ChallengeMapper;
import com.course.securityagent.mapper.ChallengeSubmitMapper;
import com.course.securityagent.mapper.SecurityKnowledgeMapper;
import com.course.securityagent.mapper.UserAchievementMapper;
import com.course.securityagent.mapper.UserMapper;
import com.course.securityagent.mapper.UserScoreMapper;
import com.course.securityagent.service.AuditLogService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeMapper challengeMapper;
    private final ChallengeSubmitMapper submitMapper;
    private final UserAchievementMapper achievementMapper;
    private final UserScoreMapper scoreMapper;
    private final UserMapper userMapper;
    private final SecurityKnowledgeMapper knowledgeMapper;
    private final AuditLogService auditLogService;

    public ChallengeController(ChallengeMapper challengeMapper,
                               ChallengeSubmitMapper submitMapper,
                               UserAchievementMapper achievementMapper,
                               UserScoreMapper scoreMapper,
                               UserMapper userMapper,
                               SecurityKnowledgeMapper knowledgeMapper,
                               AuditLogService auditLogService) {
        this.challengeMapper = challengeMapper;
        this.submitMapper = submitMapper;
        this.achievementMapper = achievementMapper;
        this.scoreMapper = scoreMapper;
        this.userMapper = userMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<ChallengeDTO>> page(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String difficulty,
                                                      @RequestParam(defaultValue = "1") Long page,
                                                      @RequestParam(defaultValue = "12") Long size) {
        LambdaQueryWrapper<Challenge> wrapper = new LambdaQueryWrapper<Challenge>()
                .eq(Challenge::getStatus, "ENABLED")
                .orderByAsc(Challenge::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Challenge::getTitle, keyword).or().like(Challenge::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Challenge::getCategory, category);
        }
        if (StringUtils.hasText(difficulty)) {
            wrapper.eq(Challenge::getDifficulty, difficulty);
        }
        Page<Challenge> result = challengeMapper.selectPage(new Page<>(page, size), wrapper);
        List<ChallengeDTO> records = result.getRecords().stream().map(this::toDto).toList();
        return ApiResponse.ok(new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChallengeDTO> detail(@PathVariable Long id) {
        Challenge challenge = enabledChallenge(id);
        auditLogService.record("START_CHALLENGE", "趣味靶场", "查看题目：" + challenge.getTitle(), "SUCCESS");
        return ApiResponse.ok(toDto(challenge));
    }

    @GetMapping("/{id}/hint")
    public ApiResponse<Map<String, String>> hint(@PathVariable Long id) {
        Challenge challenge = enabledChallenge(id);
        auditLogService.record("VIEW_CHALLENGE_HINT", "趣味靶场", "查看提示：" + challenge.getTitle(), "SUCCESS");
        return ApiResponse.ok(Map.of("hint", value(challenge.getHint())));
    }

    @GetMapping("/{id}/analysis")
    public ApiResponse<Map<String, Object>> analysis(@PathVariable Long id) {
        return agentAnalyze(id);
    }

    @PostMapping("/{id}/agent-analyze")
    public ApiResponse<Map<String, Object>> agentAnalyze(@PathVariable Long id) {
        Challenge challenge = enabledChallenge(id);
        SecurityKnowledge knowledge = challenge.getKnowledgeId() == null ? null : knowledgeMapper.selectById(challenge.getKnowledgeId());
        boolean solved = solved(challenge.getId(), UserContext.getUserId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskUnderstanding", "这是一个“" + challenge.getCategory() + "”类型的本地演示靶场题，目标是理解风险点而不是攻击真实系统。");
        data.put("keyClues", List.of(
                "观察题目给出的模拟接口和请求样例",
                "关注响应中的异常字段、身份参数或权限参数",
                "结合关联知识库判断风险成因"
        ));
        data.put("recommendedApproach", "先阅读题目背景，再查看提示和模拟请求。若接口涉及 userId、orderId、role、token 等字段，应重点判断是否由前端可控。");
        data.put("relatedKnowledge", knowledge == null ? "暂无关联知识库" : knowledge.getTitle());
        data.put("riskNote", riskNote(challenge.getCategory()));
        data.put("finalAnswerVisible", solved);
        data.put("answerPolicy", solved ? "用户已答对，可以查看完整解析。" : "为保留挑战趣味，Agent 只给思路，不直接给最终答案。");
        auditLogService.record("CHALLENGE_AGENT_ANALYZE", "趣味靶场", "Agent 分析题目：" + challenge.getTitle(), "SUCCESS");
        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/submit")
    @Transactional
    public ApiResponse<ChallengeSubmitResponse> submit(@PathVariable Long id, @RequestBody ChallengeSubmitRequest request) {
        Challenge challenge = enabledChallenge(id);
        String answer = request == null ? "" : value(request.getAnswer()).trim();
        boolean correct = challenge.getAnswerHash().equals(PasswordUtil.hash(answer));
        boolean alreadySolved = solved(challenge.getId(), UserContext.getUserId());
        int scoreGot = correct && !alreadySolved ? safeScore(challenge) : 0;
        ChallengeSubmit submit = new ChallengeSubmit();
        submit.setChallengeId(challenge.getId());
        submit.setUserId(UserContext.getUserId());
        submit.setUsername(currentUsername());
        submit.setSubmitAnswer(answer);
        submit.setCorrect(correct ? 1 : 0);
        submit.setScoreGot(scoreGot);
        submit.setUsedHint(request != null && Boolean.TRUE.equals(request.getUsedHint()) ? 1 : 0);
        submit.setSubmitTime(LocalDateTime.now());
        submitMapper.insert(submit);
        UserScore score = refreshScore(UserContext.getUserId(), submit.getUsername());
        List<UserAchievement> unlocked = correct ? unlockAchievements(UserContext.getUserId(), challenge, score.getSolvedCount()) : List.of();

        ChallengeSubmitResponse response = new ChallengeSubmitResponse();
        response.setCorrect(correct);
        response.setScoreGot(scoreGot);
        response.setTotalScore(score.getTotalScore());
        response.setSolvedCount(score.getSolvedCount());
        response.setUnlockedAchievements(unlocked);
        response.setExplanation(correct ? challenge.getExplanation() : null);
        response.setMessage(correct
                ? (scoreGot > 0 ? "挑战成功！积分已到账。" : "挑战成功！该题此前已通过，不重复加分。")
                : "答案不正确，可以查看提示再试试。");
        auditLogService.record(correct ? "CHALLENGE_SUCCESS" : "CHALLENGE_FAILED", "趣味靶场",
                (correct ? "答题成功：" : "答题失败：") + challenge.getTitle(), correct ? "SUCCESS" : "FAILED");
        return ApiResponse.ok(response);
    }

    @GetMapping("/my/stats")
    public ApiResponse<Map<String, Object>> myStats() {
        Long userId = UserContext.getUserId();
        UserScore score = refreshScore(userId, currentUsername());
        List<ChallengeSubmit> submissions = submitMapper.selectList(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getUserId, userId)
                .orderByDesc(ChallengeSubmit::getSubmitTime));
        Map<String, Long> typeMastery = new LinkedHashMap<>();
        Map<String, Integer> scoreTrend = new LinkedHashMap<>();
        Set<Long> solvedIds = submissions.stream()
                .filter(item -> item.getCorrect() != null && item.getCorrect() == 1)
                .map(ChallengeSubmit::getChallengeId)
                .collect(Collectors.toSet());
        if (!solvedIds.isEmpty()) {
            challengeMapper.selectList(new LambdaQueryWrapper<Challenge>().in(Challenge::getId, solvedIds))
                    .forEach(challenge -> typeMastery.merge(challenge.getCategory(), 1L, Long::sum));
        }
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int dayScore = submissions.stream()
                    .filter(item -> item.getSubmitTime() != null && item.getSubmitTime().toLocalDate().equals(date))
                    .map(ChallengeSubmit::getScoreGot)
                    .filter(v -> v != null)
                    .mapToInt(Integer::intValue)
                    .sum();
            scoreTrend.put(date.toString(), dayScore);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("score", score);
        data.put("totalScore", score.getTotalScore());
        data.put("solvedCount", score.getSolvedCount());
        data.put("submitCount", score.getSubmitCount());
        data.put("correctRate", score.getCorrectRate());
        data.put("streak", currentStreak(submissions));
        data.put("recentSubmissions", submissions.stream().limit(8).toList());
        data.put("achievements", achievements(userId));
        data.put("typeMastery", typeMastery);
        data.put("scoreTrend", scoreTrend);
        return ApiResponse.ok(data);
    }

    @GetMapping("/my/submissions")
    public ApiResponse<PageResult<ChallengeSubmit>> mySubmissions(@RequestParam(defaultValue = "1") Long page,
                                                                  @RequestParam(defaultValue = "10") Long size) {
        Page<ChallengeSubmit> result = submitMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getUserId, UserContext.getUserId())
                .orderByDesc(ChallengeSubmit::getSubmitTime));
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/my/achievements")
    public ApiResponse<List<UserAchievement>> myAchievements() {
        return ApiResponse.ok(achievements(UserContext.getUserId()));
    }

    private Challenge enabledChallenge(Long id) {
        Challenge challenge = challengeMapper.selectById(id);
        if (challenge == null || !"ENABLED".equals(challenge.getStatus())) {
            throw new IllegalArgumentException("题目不存在或已停用");
        }
        return challenge;
    }

    private ChallengeDTO toDto(Challenge challenge) {
        boolean solved = solved(challenge.getId(), UserContext.getUserId());
        long submitCount = submitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, challenge.getId()));
        long correctCount = submitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, challenge.getId())
                .eq(ChallengeSubmit::getCorrect, 1));
        return ChallengeDTO.from(challenge, solved, submitCount, correctCount, solved || UserContext.isAdmin());
    }

    private boolean solved(Long challengeId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = submitMapper.selectCount(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getChallengeId, challengeId)
                .eq(ChallengeSubmit::getUserId, userId)
                .eq(ChallengeSubmit::getCorrect, 1));
        return count != null && count > 0;
    }

    private UserScore refreshScore(Long userId, String username) {
        List<ChallengeSubmit> submissions = submitMapper.selectList(new LambdaQueryWrapper<ChallengeSubmit>()
                .eq(ChallengeSubmit::getUserId, userId));
        int submitCount = submissions.size();
        int totalScore = submissions.stream().map(ChallengeSubmit::getScoreGot).filter(v -> v != null).mapToInt(Integer::intValue).sum();
        int solvedCount = (int) submissions.stream()
                .filter(item -> item.getCorrect() != null && item.getCorrect() == 1)
                .map(ChallengeSubmit::getChallengeId)
                .distinct()
                .count();
        long correctSubmits = submissions.stream().filter(item -> item.getCorrect() != null && item.getCorrect() == 1).count();
        UserScore score = scoreMapper.selectOne(new LambdaQueryWrapper<UserScore>().eq(UserScore::getUserId, userId).last("limit 1"));
        if (score == null) {
            score = new UserScore();
            score.setUserId(userId);
            score.setUsername(username);
        }
        score.setTotalScore(totalScore);
        score.setSolvedCount(solvedCount);
        score.setSubmitCount(submitCount);
        score.setCorrectRate(submitCount == 0 ? 0.0 : Math.round(correctSubmits * 1000.0 / submitCount) / 10.0);
        score.setUpdateTime(LocalDateTime.now());
        if (score.getId() == null) {
            scoreMapper.insert(score);
        } else {
            scoreMapper.updateById(score);
        }
        return score;
    }

    private List<UserAchievement> unlockAchievements(Long userId, Challenge challenge, int solvedCount) {
        List<UserAchievement> unlocked = new ArrayList<>();
        if (solvedCount >= 1) unlocked.add(unlock(userId, "FIRST_SOLVE", "初次破冰", "首次答对靶场题目", "ice"));
        if (solvedCount >= 3) unlocked.add(unlock(userId, "SECURITY_BEGINNER", "安全新手", "累计答对 3 道题", "shield"));
        if (solvedCount >= 6) unlocked.add(unlock(userId, "ARENA_MASTER", "靶场达人", "完成全部 6 道初始化题目", "crown"));
        if (challenge.getCategory() != null && (challenge.getCategory().contains("未授权") || challenge.getCategory().contains("越权"))) {
            unlocked.add(unlock(userId, "RISK_HUNTER", "风险猎手", "完成访问控制高风险相关题目", "target"));
        }
        if ("Agent 推理".equals(challenge.getCategory())) {
            unlocked.add(unlock(userId, "AGENT_OBSERVER", "Agent 观察员", "完成 Agent 推理题", "agent"));
        }
        return unlocked.stream().filter(item -> item.getId() != null).toList();
    }

    private UserAchievement unlock(Long userId, String code, String name, String desc, String icon) {
        Long count = achievementMapper.selectCount(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementCode, code));
        if (count != null && count > 0) {
            return new UserAchievement();
        }
        UserAchievement achievement = new UserAchievement();
        achievement.setUserId(userId);
        achievement.setAchievementCode(code);
        achievement.setAchievementName(name);
        achievement.setAchievementDesc(desc);
        achievement.setIcon(icon);
        achievement.setUnlockTime(LocalDateTime.now());
        achievementMapper.insert(achievement);
        return achievement;
    }

    private List<UserAchievement> achievements(Long userId) {
        return achievementMapper.selectList(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .orderByDesc(UserAchievement::getUnlockTime));
    }

    private int currentStreak(List<ChallengeSubmit> submissions) {
        int streak = 0;
        for (ChallengeSubmit item : submissions.stream().sorted(Comparator.comparing(ChallengeSubmit::getSubmitTime).reversed()).toList()) {
            if (item.getCorrect() != null && item.getCorrect() == 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private String riskNote(String category) {
        if (category == null) return "该题用于安全意识训练，请结合题目上下文判断。";
        if (category.contains("越权") || category.contains("未授权")) return "访问控制问题通常风险较高，因为它可能绕过身份或权限边界。";
        if (category.contains("弱口令")) return "认证问题应关注密码复杂度、失败锁定和默认账号治理。";
        if (category.contains("信息泄露") || category.contains("Token")) return "信息泄露会为后续攻击提供线索，应按最小必要原则返回字段。";
        if (category.contains("参数")) return "关键业务参数不能由前端决定，服务端必须二次校验。";
        return "建议结合知识库条目理解题目风险点。";
    }

    private int safeScore(Challenge challenge) {
        return challenge.getScore() == null ? 0 : challenge.getScore();
    }

    private String currentUsername() {
        User user = userMapper.selectById(UserContext.getUserId());
        return user == null ? "unknown" : user.getUsername();
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
