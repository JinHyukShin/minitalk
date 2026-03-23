package com.minitalk.domain.chat.dto;

public record TypingEvent(
    Long roomId,
    Long userId,
    String userName,
    boolean typing
) {}
