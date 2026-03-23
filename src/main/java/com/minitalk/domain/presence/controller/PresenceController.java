package com.minitalk.domain.presence.controller;

import com.minitalk.domain.presence.dto.PresenceResponse;
import com.minitalk.domain.presence.service.PresenceService;
import com.minitalk.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Presence", description = "프레즌스 API")
@RestController
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @Operation(summary = "온라인 사용자 목록")
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<PresenceResponse>>> getOnlineUsers(
        @RequestParam List<Long> userIds
    ) {
        List<PresenceResponse> response = presenceService.getOnlineUsers(userIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "특정 사용자 프레즌스 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PresenceResponse>> getPresence(
        @PathVariable Long userId
    ) {
        PresenceResponse response = presenceService.getPresence(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
