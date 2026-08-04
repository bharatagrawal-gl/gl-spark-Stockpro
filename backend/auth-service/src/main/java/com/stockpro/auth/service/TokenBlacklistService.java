package com.stockpro.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "blacklist:";
    private static final String USER_BLOCKED_PREFIX = "blocked_user:"; // ← ADD

    // Store token with TTL = remaining lifetime of the token
    public void blacklist(String token, long remainingMillis) {
        if (remainingMillis <= 0) return;
        String key = PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remainingMillis));
        log.info("Token blacklisted. TTL={}ms", remainingMillis);
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }

    // ── NEW: user-level block (for deactivate/reactivate) ──────────────────

    public void blockUser(Long userId) {
        redisTemplate.opsForValue().set(
                USER_BLOCKED_PREFIX + userId, "1", Duration.ofDays(30)
        );
        log.info("User blocked in Redis. userId={}", userId);
    }

    public void unblockUser(Long userId) {
        redisTemplate.delete(USER_BLOCKED_PREFIX + userId);
        log.info("User unblocked in Redis. userId={}", userId);
    }

    public boolean isUserBlocked(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_BLOCKED_PREFIX + userId));
    }
}