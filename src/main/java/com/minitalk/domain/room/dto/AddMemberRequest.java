package com.minitalk.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddMemberRequest(
    @NotNull(message = "멤버 ID 목록은 필수입니다.")
    List<Long> userIds
) {}
