package com.example.finalproject.service.impl;

import com.example.finalproject.service.RedisBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisBlacklistServiceImpl implements RedisBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    @Override
    public void blacklistToken(String token, long ttlInMs) {
        if (ttlInMs > 0) {
            String key = BLACKLIST_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(key, "revoked", ttlInMs, TimeUnit.MILLISECONDS);
            log.info("Blacklisted token with TTL {} ms", ttlInMs);
        } else {
            log.warn("Attempted to blacklist token that is already expired");
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    @Override
    public void removeToken(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.delete(key);
        log.info("Removed token from blacklist");
    }
}
