package com.minitalk.domain.presence.dto;

public record PresenceEvent(
    Long userId,
    String status
) {}
