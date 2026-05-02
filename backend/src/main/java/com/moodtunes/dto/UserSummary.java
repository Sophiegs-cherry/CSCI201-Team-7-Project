package com.moodtunes.dto;

public record UserSummary(
        Integer userId,
        String username,
        String displayName,
        String profilePicturePath
) {}
