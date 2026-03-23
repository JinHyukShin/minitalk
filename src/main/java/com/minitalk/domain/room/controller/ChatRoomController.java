package com.minitalk.domain.room.controller;

import com.minitalk.domain.room.dto.AddMemberRequest;
import com.minitalk.domain.room.dto.ChatRoomCreateRequest;
import com.minitalk.domain.room.dto.ChatRoomListResponse;
import com.minitalk.domain.room.dto.ChatRoomResponse;
import com.minitalk.domain.room.dto.ChatRoomUpdateRequest;
import com.minitalk.domain.room.service.ChatRoomService;
import com.minitalk.global.common.ApiResponse;
import com.minitalk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ChatRoom", description = "채팅방 API")
@RestController
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Operation(summary = "채팅방 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatRoomService.createRoom(userDetails.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "내 채팅방 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getMyRooms(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ChatRoomListResponse> response = chatRoomService.getMyRooms(userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "채팅방 상세 조회")
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId
    ) {
        ChatRoomResponse response = chatRoomService.getRoom(roomId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "채팅방 설정 수정")
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> updateRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @RequestBody ChatRoomUpdateRequest request
    ) {
        ChatRoomResponse response = chatRoomService.updateRoom(roomId, userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "멤버 추가")
    @PostMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<Void>> addMembers(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @Valid @RequestBody AddMemberRequest request
    ) {
        chatRoomService.addMembers(roomId, userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "멤버 제거")
    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @PathVariable Long userId
    ) {
        chatRoomService.removeMember(roomId, userDetails.id(), userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "채팅방 나가기")
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId
    ) {
        chatRoomService.leaveRoom(roomId, userDetails.id());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
