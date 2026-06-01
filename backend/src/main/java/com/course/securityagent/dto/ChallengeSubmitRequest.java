package com.course.securityagent.dto;

public class ChallengeSubmitRequest {
    private String answer;
    private Boolean usedHint;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Boolean getUsedHint() {
        return usedHint;
    }

    public void setUsedHint(Boolean usedHint) {
        this.usedHint = usedHint;
    }
}
