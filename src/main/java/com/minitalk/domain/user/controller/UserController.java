package com.minitalk.domain.user.controller;

import com.minitalk.domain.user.dto.UserProfileResponse;
import com.minitalk.domain.user.dto.UserSearchResponse;
import com.minitalk.domain.user.dto.UserUpdateRequest;
import com.minitalk.domain.user.service.UserService;
import com.minitalk.global.common.ApiResponse;
import com.minitalk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResponse response = userService.getMyProfile(userDetails.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로필 수정")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
        @RequestParam String q
    ) {
        List<UserSearchResponse> response = userService.searchUsers(q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 프로필 조회")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
        @PathVariable Long userId
    ) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
