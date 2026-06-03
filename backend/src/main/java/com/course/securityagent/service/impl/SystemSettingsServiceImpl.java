package com.course.securityagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.securityagent.dto.SystemSettingsDTO;
import com.course.securityagent.entity.SystemConfig;
import com.course.securityagent.mapper.SystemConfigMapper;
import com.course.securityagent.service.SystemSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingsServiceImpl implements SystemSettingsService {
    private static final String PLATFORM_NAME = "platform_name";
    private static final String AGENT_MODE = "agent_mode";
    private static final String DEFAULT_DEPTH = "default_depth";
    private static final String AUTO_REPORT = "auto_report";
    private static final String ENABLE_REGISTER = "enable_register";
    private static final String SECURITY_STATEMENT = "security_statement";

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        DEFAULTS.put(PLATFORM_NAME, "自主安全测试智能体可视化平台");
        DEFAULTS.put(AGENT_MODE, "规则模拟模式");
        DEFAULTS.put(DEFAULT_DEPTH, "标准");
        DEFAULTS.put(AUTO_REPORT, "true");
        DEFAULTS.put(ENABLE_REGISTER, "true");
        DEFAULTS.put(SECURITY_STATEMENT, "本平台仅用于本地靶场、自建测试系统、课程演示环境或已授权目标，不执行未授权攻击或破坏性扫描。");

        LABELS.put(PLATFORM_NAME, "平台名称");
        LABELS.put(AGENT_MODE, "Agent 模式");
        LABELS.put(DEFAULT_DEPTH, "默认测试深度");
        LABELS.put(AUTO_REPORT, "自动生成报告");
        LABELS.put(ENABLE_REGISTER, "开启注册");
        LABELS.put(SECURITY_STATEMENT, "安全声明");
    }

    private final SystemConfigMapper configMapper;

    public SystemSettingsServiceImpl(SystemConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public SystemSettingsDTO getSettings() {
        ensureDefaults();
        Map<String, String> values = values();
        SystemSettingsDTO dto = new SystemSettingsDTO();
        dto.setPlatformName(values.get(PLATFORM_NAME));
        dto.setAgentMode(values.get(AGENT_MODE));
        dto.setDefaultDepth(values.get(DEFAULT_DEPTH));
        dto.setAutoReport(bool(values.get(AUTO_REPORT)));
        dto.setEnableRegister(bool(values.get(ENABLE_REGISTER)));
        dto.setStatement(values.get(SECURITY_STATEMENT));
        return dto;
    }

    @Override
    public SystemSettingsDTO saveSettings(SystemSettingsDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("配置内容不能为空");
        }
        upsert(PLATFORM_NAME, text(request.getPlatformName(), DEFAULTS.get(PLATFORM_NAME)));
        upsert(AGENT_MODE, text(request.getAgentMode(), DEFAULTS.get(AGENT_MODE)));
        upsert(DEFAULT_DEPTH, text(request.getDefaultDepth(), DEFAULTS.get(DEFAULT_DEPTH)));
        upsert(AUTO_REPORT, String.valueOf(Boolean.TRUE.equals(request.getAutoReport())));
        upsert(ENABLE_REGISTER, String.valueOf(Boolean.TRUE.equals(request.getEnableRegister())));
        upsert(SECURITY_STATEMENT, text(request.getStatement(), DEFAULTS.get(SECURITY_STATEMENT)));
        return getSettings();
    }

    @Override
    public SystemSettingsDTO resetDefaults() {
        DEFAULTS.forEach(this::upsert);
        return getSettings();
    }

    @Override
    public boolean isRegisterEnabled() {
        return Boolean.TRUE.equals(getSettings().getEnableRegister());
    }

    private void ensureDefaults() {
        DEFAULTS.forEach((key, value) -> {
            Long count = configMapper.selectCount(new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, key));
            if (count == null || count == 0) {
                insert(key, value);
            }
        });
    }

    private Map<String, String> values() {
        List<SystemConfig> configs = configMapper.selectList(new LambdaQueryWrapper<SystemConfig>()
                .in(SystemConfig::getConfigKey, DEFAULTS.keySet()));
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS);
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    private void upsert(String key, String value) {
        SystemConfig config = configMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key)
                .last("limit 1"));
        if (config == null) {
            insert(key, value);
            return;
        }
        config.setConfigValue(value);
        config.setConfigLabel(LABELS.getOrDefault(key, key));
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
    }

    private void insert(String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigLabel(LABELS.getOrDefault(key, key));
        config.setUpdateTime(LocalDateTime.now());
        configMapper.insert(config);
    }

    private boolean bool(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String text(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
