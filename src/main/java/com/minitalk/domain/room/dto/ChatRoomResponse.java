package com.minitalk.domain.room.dto;

import com.minitalk.domain.room.entity.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
    Long id,
    String type,
    String name,
    String iconUrl,
    List<MemberInfo> members,
    LocalDateTime createdAt
) {

    public record MemberInfo(
        Long userId,
        String name,
        String avatarUrl,
        boolean online
    ) {}

    public static ChatRoomResponse from(ChatRoom room, List<MemberInfo> members) {
        return new ChatRoomResponse(
            room.getId(),
            room.getType().name(),
            room.getName(),
            room.getIconUrl(),
            members,
            room.getCreatedAt()
        );
    }
}
