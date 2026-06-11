package com.example.finalproject.service.impl;

import com.example.finalproject.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ATTEMPT_KEY_PREFIX = "login_attempt:";
    private static final String LOCK_KEY_PREFIX = "login_lock:";
    private static final String LOCKED_VALUE = "LOCKED";
    private static final int MAX_ATTEMPTS = 5;
    private static final long ATTEMPT_TTL_SECONDS = 60; // 1 minute
    private static final long LOCK_TTL_SECONDS = 300; // 5 minutes

    @Override
    public boolean isLocked(String email) {
        String lockKey = LOCK_KEY_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    @Override
    public void failAttempt(String email) {
        if (isLocked(email)) {
            return;
        }

        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null) {
            if (attempts == 1) {
                redisTemplate.expire(attemptKey, Duration.ofSeconds(ATTEMPT_TTL_SECONDS));
            }

            if (attempts >= MAX_ATTEMPTS) {
                String lockKey = LOCK_KEY_PREFIX + email;
                redisTemplate.opsForValue().set(lockKey, LOCKED_VALUE, Duration.ofSeconds(LOCK_TTL_SECONDS));
                // Once locked, we can optionally clean up the attempts key
                redisTemplate.delete(attemptKey);
            }
        }
    }

    @Override
    public void resetAttempts(String email) {
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        String lockKey = LOCK_KEY_PREFIX + email;
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockKey);
    }
}
