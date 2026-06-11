package com.example.finalproject.service.impl;

import com.example.finalproject.exception.BusinessException;
import com.example.finalproject.exception.InvalidOtpException;
import com.example.finalproject.exception.OtpAttemptExceededException;
import com.example.finalproject.exception.OtpExpiredException;
import com.example.finalproject.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisOtpServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RedisOtpService redisOtpService;

    private final String email = "lecturer@gmail.com";
    private final String otpKey = "otp:lecturer:" + email;
    private final String attemptKey = "otp_attempt:" + email;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateOtp_Success() {
        String otp = redisOtpService.generateOtp(email);

        assertNotNull(otp);
        assertEquals(6, otp.length());

        verify(redisTemplate, times(1)).delete(attemptKey);
        verify(valueOperations, times(1)).set(eq(otpKey), eq(otp), eq(120L), eq(TimeUnit.SECONDS));
    }

    @Test
    void verifyOtp_Success() {
        when(valueOperations.get(attemptKey)).thenReturn(null);
        when(valueOperations.get(otpKey)).thenReturn("123456");

        boolean result = redisOtpService.verifyOtp(email, "123456");

        assertTrue(result);
        verify(redisTemplate, times(1)).delete(otpKey);
        verify(redisTemplate, times(1)).delete(attemptKey);
    }

    @Test
    void verifyOtp_Expired_ThrowsOtpExpiredException() {
        when(valueOperations.get(attemptKey)).thenReturn("1");
        when(valueOperations.get(otpKey)).thenReturn(null);

        OtpExpiredException exception = assertThrows(OtpExpiredException.class, 
                () -> redisOtpService.verifyOtp(email, "123456"));
        assertEquals("OTP has expired or does not exist", exception.getMessage());
    }

    @Test
    void verifyOtp_BlockedBeforeVerify_ThrowsOtpAttemptExceededException() {
        when(valueOperations.get(attemptKey)).thenReturn("5");

        OtpAttemptExceededException exception = assertThrows(OtpAttemptExceededException.class, 
                () -> redisOtpService.verifyOtp(email, "123456"));
        assertEquals("OTP session blocked. Please request a new OTP.", exception.getMessage());
        verify(redisTemplate, times(1)).delete(otpKey);
    }

    @Test
    void verifyOtp_WrongOtp_IncrementsAttempt() {
        when(valueOperations.get(attemptKey)).thenReturn("2");
        when(valueOperations.get(otpKey)).thenReturn("123456");
        when(valueOperations.increment(attemptKey)).thenReturn(3L);

        InvalidOtpException exception = assertThrows(InvalidOtpException.class, 
                () -> redisOtpService.verifyOtp(email, "654321"));
        assertEquals("Incorrect OTP. Remaining attempts: 2", exception.getMessage());
    }

    @Test
    void verifyOtp_WrongOtp_FirstAttempt_SetsExpiry() {
        when(valueOperations.get(attemptKey)).thenReturn(null);
        when(valueOperations.get(otpKey)).thenReturn("123456");
        when(valueOperations.increment(attemptKey)).thenReturn(1L);

        InvalidOtpException exception = assertThrows(InvalidOtpException.class, 
                () -> redisOtpService.verifyOtp(email, "654321"));
        assertEquals("Incorrect OTP. Remaining attempts: 4", exception.getMessage());
        verify(redisTemplate, times(1)).expire(attemptKey, 120, TimeUnit.SECONDS);
    }

    @Test
    void verifyOtp_ExceedsLimit_BlocksOtp() {
        when(valueOperations.get(attemptKey)).thenReturn("4");
        when(valueOperations.get(otpKey)).thenReturn("123456");
        when(valueOperations.increment(attemptKey)).thenReturn(5L);

        OtpAttemptExceededException exception = assertThrows(OtpAttemptExceededException.class, 
                () -> redisOtpService.verifyOtp(email, "654321"));
        assertEquals("Incorrect OTP. Attempt limit exceeded. OTP session blocked.", exception.getMessage());
        verify(redisTemplate, times(1)).delete(otpKey);
    }

    @Test
    void resendOtp_StillValid_ThrowsBusinessException() {
        when(valueOperations.get(otpKey)).thenReturn("123456");
        when(valueOperations.get(attemptKey)).thenReturn("1");

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> redisOtpService.resendOtp(email));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Current OTP is still valid. Please wait or check your email.", exception.getMessage());
    }

    @Test
    void resendOtp_Expired_GeneratesAndSendsOtp() {
        when(valueOperations.get(otpKey)).thenReturn(null);
        when(valueOperations.get(attemptKey)).thenReturn("1");

        redisOtpService.resendOtp(email);

        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void resendOtp_Blocked_GeneratesAndSendsOtp() {
        when(valueOperations.get(otpKey)).thenReturn("123456");
        when(valueOperations.get(attemptKey)).thenReturn("5");

        redisOtpService.resendOtp(email);

        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }
}
