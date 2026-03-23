package com.minitalk.domain.chat.controller;

import com.minitalk.domain.chat.dto.ChatMessageResponse;
import com.minitalk.domain.chat.dto.MessageEditRequest;
import com.minitalk.domain.chat.service.ChatService;
import com.minitalk.domain.room.service.ChatRoomService;
import com.minitalk.global.common.ApiResponse;
import com.minitalk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "채팅 메시지 API")
@RestController
@RequestMapping("/api/v1")
public class ChatRestController {

    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    public ChatRestController(ChatService chatService, ChatRoomService chatRoomService) {
        this.chatService = chatService;
        this.chatRoomService = chatRoomService;
    }

    @Operation(summary = "메시지 히스토리 조회 (커서 기반 페이징)")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "50") int size
    ) {
        chatRoomService.validateMembership(roomId, userDetails.id());
        List<ChatMessageResponse> messages = chatService.getMessages(roomId, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "메시지 검색")
    @GetMapping("/rooms/{roomId}/messages/search")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> searchMessages(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        chatRoomService.validateMembership(roomId, userDetails.id());
        List<ChatMessageResponse> messages = chatService.searchMessages(roomId, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "메시지 수정")
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> editMessage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String messageId,
        @Valid @RequestBody MessageEditRequest request
    ) {
        ChatMessageResponse response = chatService.editMessage(messageId, userDetails.id(), request.content());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메시지 삭제")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String messageId
    ) {
        chatService.deleteMessage(messageId, userDetails.id());
        return ResponseEntity.noContent().build();
    }
}
