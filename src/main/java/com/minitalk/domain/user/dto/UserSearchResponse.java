package com.minitalk.domain.user.dto;

import com.minitalk.domain.auth.entity.User;

public record UserSearchResponse(
    Long id,
    String name,
    String email,
    String avatarUrl
) {

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatarUrl()
        );
    }
}
