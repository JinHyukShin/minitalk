package com.minitalk.domain.call.controller;

import com.minitalk.domain.call.dto.CallResponse;
import com.minitalk.domain.call.dto.CallStartRequest;
import com.minitalk.domain.call.service.CallService;
import com.minitalk.global.common.ApiResponse;
import com.minitalk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Call", description = "통화 API")
@RestController
@RequestMapping("/api/v1/calls")
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    @Operation(summary = "통화 시작")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<CallResponse>> startCall(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody CallStartRequest request
    ) {
        CallResponse response = callService.startCall(userDetails.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "통화 수락")
    @PostMapping("/{callId}/accept")
    public ResponseEntity<ApiResponse<CallResponse>> acceptCall(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long callId
    ) {
        CallResponse response = callService.acceptCall(callId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "통화 거절")
    @PostMapping("/{callId}/reject")
    public ResponseEntity<ApiResponse<CallResponse>> rejectCall(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long callId
    ) {
        CallResponse response = callService.rejectCall(callId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "통화 종료")
    @PostMapping("/{callId}/end")
    public ResponseEntity<ApiResponse<CallResponse>> endCall(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long callId
    ) {
        CallResponse response = callService.endCall(callId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "그룹 통화 참여")
    @PostMapping("/{callId}/join")
    public ResponseEntity<ApiResponse<CallResponse>> joinCall(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long callId
    ) {
        CallResponse response = callService.joinGroupCall(callId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "통화 이력 조회")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getCallHistory(
        @PathVariable Long roomId
    ) {
        List<CallResponse> response = callService.getCallHistory(roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
