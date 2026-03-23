package com.minitalk.domain.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하입니다.")
    String name,

    @Size(max = 200, message = "상태 메시지는 200자 이하입니다.")
    String statusMessage
) {}
