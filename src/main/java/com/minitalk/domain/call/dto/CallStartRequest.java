package com.minitalk.domain.call.dto;

import jakarta.validation.constraints.NotNull;

public record CallStartRequest(
    @NotNull(message = "채팅방 ID는 필수입니다.")
    Long roomId,

    @NotNull(message = "통화 타입은 필수입니다.")
    String callType
) {}
