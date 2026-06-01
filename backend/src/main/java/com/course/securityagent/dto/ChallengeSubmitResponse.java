package com.course.securityagent.dto;

import com.course.securityagent.entity.UserAchievement;

import java.util.List;

public class ChallengeSubmitResponse {
    private boolean correct;
    private int scoreGot;
    private int totalScore;
    private int solvedCount;
    private String message;
    private String explanation;
    private List<UserAchievement> unlockedAchievements;

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScoreGot() {
        return scoreGot;
    }

    public void setScoreGot(int scoreGot) {
        this.scoreGot = scoreGot;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<UserAchievement> getUnlockedAchievements() {
        return unlockedAchievements;
    }

    public void setUnlockedAchievements(List<UserAchievement> unlockedAchievements) {
        this.unlockedAchievements = unlockedAchievements;
    }
}
