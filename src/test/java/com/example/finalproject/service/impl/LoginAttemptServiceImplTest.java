package com.example.finalproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginAttemptServiceImpl loginAttemptService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void isLocked_True() {
        String email = "test@gmail.com";
        when(redisTemplate.hasKey("login_lock:" + email)).thenReturn(true);

        assertTrue(loginAttemptService.isLocked(email));
        verify(redisTemplate, times(1)).hasKey("login_lock:" + email);
    }

    @Test
    void isLocked_False() {
        String email = "test@gmail.com";
        when(redisTemplate.hasKey("login_lock:" + email)).thenReturn(false);

        assertFalse(loginAttemptService.isLocked(email));
        verify(redisTemplate, times(1)).hasKey("login_lock:" + email);
    }

    @Test
    void failAttempt_FirstFail() {
        String email = "test@gmail.com";
        String attemptKey = "login_attempt:" + email;
        
        when(redisTemplate.hasKey("login_lock:" + email)).thenReturn(false);
        when(valueOperations.increment(attemptKey)).thenReturn(1L);

        loginAttemptService.failAttempt(email);

        verify(valueOperations, times(1)).increment(attemptKey);
        verify(redisTemplate, times(1)).expire(eq(attemptKey), any(Duration.class));
    }

    @Test
    void failAttempt_LockTriggered() {
        String email = "test@gmail.com";
        String attemptKey = "login_attempt:" + email;
        String lockKey = "login_lock:" + email;

        when(redisTemplate.hasKey(lockKey)).thenReturn(false);
        when(valueOperations.increment(attemptKey)).thenReturn(5L);

        loginAttemptService.failAttempt(email);

        verify(valueOperations, times(1)).increment(attemptKey);
        verify(valueOperations, times(1)).set(eq(lockKey), eq("LOCKED"), any(Duration.class));
        verify(redisTemplate, times(1)).delete(attemptKey);
    }

    @Test
    void resetAttempts() {
        String email = "test@gmail.com";
        String attemptKey = "login_attempt:" + email;
        String lockKey = "login_lock:" + email;

        loginAttemptService.resetAttempts(email);

        verify(redisTemplate, times(1)).delete(attemptKey);
        verify(redisTemplate, times(1)).delete(lockKey);
    }
}
