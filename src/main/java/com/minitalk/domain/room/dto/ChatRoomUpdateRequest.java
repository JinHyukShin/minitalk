package com.minitalk.domain.room.dto;

public record ChatRoomUpdateRequest(
    String name,
    String iconUrl
) {}
