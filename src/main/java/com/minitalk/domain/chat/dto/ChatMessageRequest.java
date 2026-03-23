package com.minitalk.domain.chat.dto;

public record ChatMessageRequest(
    String content,
    String type
) {}
