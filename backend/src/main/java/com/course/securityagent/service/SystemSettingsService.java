package com.course.securityagent.service;

import com.course.securityagent.dto.SystemSettingsDTO;

public interface SystemSettingsService {
    SystemSettingsDTO getSettings();

    SystemSettingsDTO saveSettings(SystemSettingsDTO request);

    SystemSettingsDTO resetDefaults();

    boolean isRegisterEnabled();
}
