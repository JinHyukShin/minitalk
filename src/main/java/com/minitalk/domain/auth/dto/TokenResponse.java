package com.minitalk.domain.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    Long userId,
    String name,
    String email
) {}
