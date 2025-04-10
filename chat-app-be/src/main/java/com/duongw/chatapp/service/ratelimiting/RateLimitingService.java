package com.duongw.chatapp.service.ratelimiting;

import com.duongw.chatapp.model.enums.RateLimitingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitingService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_PASSWORD_RESET_ATTEMPTS = 3;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_VERIFY_CODE_ATTEMPTS = 10;
    private static final long PASSWORD_RESET_TTL_SECONDS = 3600; // 1 hour

    private String generateRedisKey(RateLimitingType type, String key) {
        return "rate_limit:" + type.name() + ":" + key;
    }

    private int getMaxAttempts(RateLimitingType type) {
        return switch (type) {
            case PASSWORD_RESET -> MAX_PASSWORD_RESET_ATTEMPTS;
            case LOGIN_ATTEMPT -> MAX_LOGIN_ATTEMPTS;
            case VERIFICATION_EMAIL -> MAX_VERIFY_CODE_ATTEMPTS;
            default -> 10;
        };
    }

    private long getTTL(RateLimitingType type) {
        return switch (type) {
            case PASSWORD_RESET -> PASSWORD_RESET_TTL_SECONDS;
            // Thêm các case khác...
            default -> 3600;
        };
    }


    public boolean isExceeded(RateLimitingType type, String key) {
        String redisKey = generateRedisKey(type, key);

        // Lấy số lần hiện tại
        Integer attempts = (Integer) redisTemplate.opsForValue().get(redisKey);

        if (attempts == null) {
            // Tạo mới nếu chưa có
            redisTemplate.opsForValue().set(redisKey, 1, getTTL(type), TimeUnit.SECONDS);
            return false;
        }

        if (attempts >= getMaxAttempts(type)) {
            return true; // Vượt quá giới hạn
        }

        // Tăng số lần
        redisTemplate.opsForValue().increment(redisKey);
        return false;
    }

}
