package com.course.securityagent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_achievement")
public class UserAchievement {
    private Long id;
    private Long userId;
    private String achievementCode;
    private String achievementName;
    private String achievementDesc;
    private String icon;
    private LocalDateTime unlockTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAchievementCode() {
        return achievementCode;
    }

    public void setAchievementCode(String achievementCode) {
        this.achievementCode = achievementCode;
    }

    public String getAchievementName() {
        return achievementName;
    }

    public void setAchievementName(String achievementName) {
        this.achievementName = achievementName;
    }

    public String getAchievementDesc() {
        return achievementDesc;
    }

    public void setAchievementDesc(String achievementDesc) {
        this.achievementDesc = achievementDesc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDateTime getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(LocalDateTime unlockTime) {
        this.unlockTime = unlockTime;
    }
}
