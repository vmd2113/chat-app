package com.duongw.chatapp.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j

public class RedisCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * lưu giá trị vào cache với thời gian hết hạn
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Cached value: {} with key: {} and timeout: {}", value, key, timeout);
        } catch (Exception e) {
            log.error("Error setting value in Redis: {}", e.getMessage());
        }
    }

    /**
     * lấy giá trị từ cache
     */

    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Retrieved value: {} with key: {}", value, key);
            } else {
                log.debug("No value found for key: {}", key);
            }
            return value;

        } catch (Exception e) {

            log.error("Error getting value from Redis: {}", e.getMessage());
            return null;
        }

    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted value with key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting value from Redis: {}", e.getMessage());

        }
    }

    public void deleteByPattern(String keyPattern) {
        try {
            Set<String> keys = redisTemplate.keys(keyPattern + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    redisTemplate.delete(key);
                    log.debug("Deleted value with key: {}", key);
                }
            } else {
                log.debug("No keys found for pattern: {}", keyPattern);
            }
        } catch (Exception e) {
            log.error("Error deleting value from Redis: {}", e.getMessage());
        }
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking existence of key: {}", key, e);
            return false;
        }
    }

    /**
     * gia hạn cho key*/

    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }
}
