package com.minitalk.domain.auth.service;

import com.minitalk.domain.auth.dto.LoginRequest;
import com.minitalk.domain.auth.dto.SignupRequest;
import com.minitalk.domain.auth.dto.TokenRequest;
import com.minitalk.domain.auth.dto.TokenResponse;
import com.minitalk.domain.auth.entity.RefreshToken;
import com.minitalk.domain.auth.entity.User;
import com.minitalk.domain.auth.repository.RefreshTokenRepository;
import com.minitalk.domain.auth.repository.UserRepository;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import com.minitalk.global.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATE);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), encodedPassword, request.name());
        userRepository.save(user);

        return generateTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return generateTokens(user);
    }

    @Transactional
    public TokenResponse refresh(TokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(refreshToken);

        return generateTokens(user);
    }

    @Transactional
    public void logout(String accessToken, TokenRequest request) {
        long remainingExpiry = jwtProvider.getRemainingExpiry(accessToken);
        if (remainingExpiry > 0) {
            redisTemplate.opsForValue().set(
                "jwt:blacklist:" + accessToken, "1",
                remainingExpiry, TimeUnit.MILLISECONDS);
        }

        if (request != null && request.refreshToken() != null) {
            refreshTokenRepository.deleteByToken(request.refreshToken());
        }
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName());
        String refreshTokenStr = jwtProvider.createRefreshToken(user.getId());

        LocalDateTime expiresAt = LocalDateTime.now()
            .plusSeconds(jwtProvider.getRefreshTokenExpiry() / 1000);
        RefreshToken refreshToken = RefreshToken.create(user.getId(), refreshTokenStr, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenStr, user.getId(), user.getName(), user.getEmail());
    }
}
