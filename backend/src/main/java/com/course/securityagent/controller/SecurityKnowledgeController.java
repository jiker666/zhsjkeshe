package com.course.securityagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.common.ForbiddenException;
import com.course.securityagent.common.PageResult;
import com.course.securityagent.common.UserContext;
import com.course.securityagent.entity.SecurityKnowledge;
import com.course.securityagent.mapper.SecurityKnowledgeMapper;
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
@RequestMapping("/api/knowledge")
public class SecurityKnowledgeController {
    private final SecurityKnowledgeMapper knowledgeMapper;

    public SecurityKnowledgeController(SecurityKnowledgeMapper knowledgeMapper) {
        this.knowledgeMapper = knowledgeMapper;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<SecurityKnowledge>> page(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String riskLevel,
                                                           @RequestParam(required = false) String vulnType,
                                                           @RequestParam(defaultValue = "1") Long page,
                                                           @RequestParam(defaultValue = "12") Long size) {
        LambdaQueryWrapper<SecurityKnowledge> wrapper = new LambdaQueryWrapper<SecurityKnowledge>()
                .orderByDesc(SecurityKnowledge::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SecurityKnowledge::getTitle, keyword)
                    .or()
                    .like(SecurityKnowledge::getDescription, keyword));
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(SecurityKnowledge::getRiskLevel, riskLevel);
        }
        if (StringUtils.hasText(vulnType)) {
            wrapper.like(SecurityKnowledge::getVulnType, vulnType);
        }
        Page<SecurityKnowledge> result = knowledgeMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SecurityKnowledge> detail(@PathVariable Long id) {
        SecurityKnowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new IllegalArgumentException("知识库记录不存在");
        }
        return ApiResponse.ok(knowledge);
    }

    @GetMapping("/by-type/{vulnType}")
    public ApiResponse<SecurityKnowledge> byType(@PathVariable String vulnType) {
        return ApiResponse.ok(knowledgeMapper.selectOne(new LambdaQueryWrapper<SecurityKnowledge>()
                .like(SecurityKnowledge::getVulnType, vulnType)
                .last("limit 1")));
    }

    @PostMapping
    public ApiResponse<SecurityKnowledge> create(@RequestBody SecurityKnowledge request) {
        requireAdmin();
        request.setCreateTime(LocalDateTime.now());
        knowledgeMapper.insert(request);
        return ApiResponse.ok(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<SecurityKnowledge> update(@PathVariable Long id, @RequestBody SecurityKnowledge request) {
        requireAdmin();
        request.setId(id);
        knowledgeMapper.updateById(request);
        return ApiResponse.ok(knowledgeMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireAdmin();
        knowledgeMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void requireAdmin() {
        if (!UserContext.isAdmin()) {
            throw new ForbiddenException("无权限访问该资源");
        }
    }
}
