package com.minitalk.domain.call.dto;

import com.minitalk.domain.call.entity.CallHistory;
import java.time.LocalDateTime;

public record CallResponse(
    Long id,
    Long roomId,
    Long callerId,
    String callType,
    String status,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Integer durationSeconds,
    LocalDateTime createdAt
) {

    public static CallResponse from(CallHistory call) {
        return new CallResponse(
            call.getId(),
            call.getRoomId(),
            call.getCallerId(),
            call.getCallType(),
            call.getStatus().name(),
            call.getStartedAt(),
            call.getEndedAt(),
            call.getDurationSeconds(),
            call.getCreatedAt()
        );
    }
}
