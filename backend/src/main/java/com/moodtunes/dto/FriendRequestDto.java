package com.moodtunes.dto;

import jakarta.validation.constraints.NotNull;

public class FriendRequestDto {
    @NotNull
    private Integer targetUserId;

    public Integer getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
}
