package com.minitalk.domain.call.dto;

public record SdpAnswerResponse(
    Long callId,
    String sdpAnswer
) {}
