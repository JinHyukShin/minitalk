package com.minitalk.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ChatRoomCreateRequest(
    @NotNull(message = "채팅방 타입은 필수입니다.")
    String type,

    String name,

    @NotNull(message = "멤버 목록은 필수입니다.")
    List<Long> memberIds
) {}
