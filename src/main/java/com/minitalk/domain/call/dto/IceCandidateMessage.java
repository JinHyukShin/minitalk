package com.minitalk.domain.call.dto;

public record IceCandidateMessage(
    Long callId,
    String candidate,
    String sdpMid,
    int sdpMLineIndex
) {}
