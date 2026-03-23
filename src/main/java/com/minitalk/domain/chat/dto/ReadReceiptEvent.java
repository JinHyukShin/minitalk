package com.minitalk.domain.chat.dto;

public record ReadReceiptEvent(
    Long roomId,
    Long userId,
    String lastMessageId
) {}
