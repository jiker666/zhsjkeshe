package com.course.securityagent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("challenge_submit")
public class ChallengeSubmit {
    private Long id;
    private Long challengeId;
    private Long userId;
    private String username;
    private String submitAnswer;
    private Integer correct;
    private Integer scoreGot;
    private Integer usedHint;
    private LocalDateTime submitTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubmitAnswer() {
        return submitAnswer;
    }

    public void setSubmitAnswer(String submitAnswer) {
        this.submitAnswer = submitAnswer;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public Integer getScoreGot() {
        return scoreGot;
    }

    public void setScoreGot(Integer scoreGot) {
        this.scoreGot = scoreGot;
    }

    public Integer getUsedHint() {
        return usedHint;
    }

    public void setUsedHint(Integer usedHint) {
        this.usedHint = usedHint;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
}
