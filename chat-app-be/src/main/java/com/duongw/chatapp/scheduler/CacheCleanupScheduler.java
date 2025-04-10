package com.duongw.chatapp.scheduler;

import com.duongw.chatapp.utils.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class CacheCleanupScheduler {
    private final RedisCacheUtil redisCacheUtil;




}
