package com.example.finalproject.service.impl;

import com.example.finalproject.exception.BusinessException;
import com.example.finalproject.exception.InvalidOtpException;
import com.example.finalproject.exception.OtpAttemptExceededException;
import com.example.finalproject.exception.OtpExpiredException;
import com.example.finalproject.service.EmailService;
import com.example.finalproject.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisOtpService implements OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_KEY_PREFIX = "otp:lecturer:";
    private static final String ATTEMPT_KEY_PREFIX = "otp_attempt:";
    private static final int OTP_TTL_SECONDS = 120;
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public String generateOtp(String email) {
        // Reset attempts count when generating a new OTP
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        redisTemplate.delete(attemptKey);

        // Generate 6 digit OTP
        int number = secureRandom.nextInt(1000000);
        String otp = String.format("%06d", number);

        String otpKey = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(otpKey, otp, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("Generated OTP for user {}. Saved in Redis with key {}", email, otpKey);
        return otp;
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String otpKey = OTP_KEY_PREFIX + email;
        String attemptKey = ATTEMPT_KEY_PREFIX + email;

        // Check attempts count first
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            // Delete OTP key just in case
            redisTemplate.delete(otpKey);
            throw new OtpAttemptExceededException("OTP session blocked. Please request a new OTP.");
        }

        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        if (storedOtp == null) {
            throw new OtpExpiredException("OTP has expired or does not exist");
        }

        if (storedOtp.equals(otp)) {
            // Success: clear Redis keys
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            return true;
        } else {
            // Failure: increment attempt count
            Long updatedAttempts = redisTemplate.opsForValue().increment(attemptKey);
            // If updatedAttempts is 1 (new key), set TTL on it
            if (updatedAttempts != null && updatedAttempts == 1) {
                redisTemplate.expire(attemptKey, OTP_TTL_SECONDS, TimeUnit.SECONDS);
            }

            int currentAttempts = updatedAttempts != null ? updatedAttempts.intValue() : attempts + 1;

            if (currentAttempts >= MAX_ATTEMPTS) {
                redisTemplate.delete(otpKey);
                throw new OtpAttemptExceededException("Incorrect OTP. Attempt limit exceeded. OTP session blocked.");
            } else {
                throw new InvalidOtpException("Incorrect OTP. Remaining attempts: " + (MAX_ATTEMPTS - currentAttempts));
            }
        }
    }

    @Override
    public void resendOtp(String email) {
        String otpKey = OTP_KEY_PREFIX + email;
        String attemptKey = ATTEMPT_KEY_PREFIX + email;

        // Check if OTP exists and not blocked
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        // "Chỉ gửi lại khi OTP hết hạn hoặc bị khóa."
        // OTP is considered expired if storedOtp == null
        // OTP is considered blocked if attempts >= MAX_ATTEMPTS
        // If it is NOT expired and NOT blocked, we do not allow resending.
        if (storedOtp != null && attempts < MAX_ATTEMPTS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Current OTP is still valid. Please wait or check your email.");
        }

        // Generate new OTP
        String newOtp = generateOtp(email);
        // Send email
        emailService.sendOtpEmail(email, newOtp);
        log.info("Resent OTP email to {}", email);
    }

    @Override
    public void clearOtp(String email) {
        redisTemplate.delete(OTP_KEY_PREFIX + email);
        redisTemplate.delete(ATTEMPT_KEY_PREFIX + email);
    }
}
