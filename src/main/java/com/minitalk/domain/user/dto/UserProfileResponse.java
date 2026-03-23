package com.minitalk.domain.user.dto;

import com.minitalk.domain.auth.entity.User;
import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String email,
    String name,
    String avatarUrl,
    String statusMessage,
    LocalDateTime createdAt
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getAvatarUrl(),
            user.getStatusMessage(),
            user.getCreatedAt()
        );
    }
}
