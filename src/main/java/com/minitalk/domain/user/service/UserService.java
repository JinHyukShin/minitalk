package com.minitalk.domain.user.service;

import com.minitalk.domain.auth.entity.User;
import com.minitalk.domain.auth.repository.UserRepository;
import com.minitalk.domain.user.dto.UserProfileResponse;
import com.minitalk.domain.user.dto.UserSearchResponse;
import com.minitalk.domain.user.dto.UserUpdateRequest;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.name(), request.statusMessage());
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String query) {
        return userRepository.searchByNameOrEmail(query).stream()
            .map(UserSearchResponse::from)
            .toList();
    }
}
