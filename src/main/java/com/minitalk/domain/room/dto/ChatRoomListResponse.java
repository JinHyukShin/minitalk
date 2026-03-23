package com.minitalk.domain.room.dto;

public record ChatRoomListResponse(
    Long roomId,
    String type,
    String name,
    LastMessageInfo lastMessage,
    int unreadCount,
    int memberCount
) {

    public record LastMessageInfo(
        String content,
        String senderName,
        String sentAt,
        String type
    ) {}
}
