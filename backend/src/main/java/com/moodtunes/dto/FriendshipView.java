package com.moodtunes.dto;

public record FriendshipView(
        Integer friendshipId,
        UserSummary user,
        String status,
        String direction
) {}
