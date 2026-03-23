package com.minitalk.domain.presence.dto;

public record PresenceResponse(
    Long userId,
    String status,
    String lastSeen
) {}
