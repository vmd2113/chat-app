package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.InvalidTokenException;
import com.duongw.chatapp.model.entity.RefreshToken;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.repository.RefreshTokenRepository;
import com.duongw.chatapp.service.IRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Users user, HttpServletRequest request) {
        // Generate a unique token
        String tokenValue = UUID.randomUUID().toString();

        // Create new token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        // Add client information if available
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIp(request);

            refreshToken.setDeviceInfo(userAgent);
            refreshToken.setIpAddress(ipAddress);
        }

        // Save to database
        log.info("Creating new refresh token for user: {}", user.getEmail());
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyExpiration(String token) {
        // Find token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found in database"));

        // Check if token is expired or revoked
        if (refreshToken.isExpired()) {
            log.warn("Refresh token expired: {}", token);
            throw new InvalidTokenException("Refresh token is expired");
        }

        if (refreshToken.getRevoked()) {
            log.warn("Refresh token revoked: {}", token);
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        log.info("Verified refresh token for user: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        // Find token
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        // Revoke if exists
        refreshTokenOpt.ifPresent(refreshToken -> {
            log.info("Revoking refresh token for user: {}", refreshToken.getUser().getEmail());
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Users user) {
        log.info("Revoking all refresh tokens for user: {}", user.getEmail());
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    /**
     * Scheduled task to clean up expired tokens
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }

    // Helper method to get client IP address
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}