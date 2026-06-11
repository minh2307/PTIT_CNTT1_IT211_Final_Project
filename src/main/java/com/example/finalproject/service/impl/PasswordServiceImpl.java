package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.ChangePasswordRequest;
import com.example.finalproject.model.dto.request.ForgotPasswordRequest;
import com.example.finalproject.model.dto.request.ResetPasswordRequest;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.MailService;
import com.example.finalproject.service.PasswordService;
import com.example.finalproject.service.RedisBlacklistService;
import com.example.finalproject.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisBlacklistService redisBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final JwtService jwtService;

    private static final String RESET_PASSWORD_KEY_PREFIX = "password_reset_token:";

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String email, String authHeader) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Old password incorrect");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "New password must be different from old password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens for the user
        refreshTokenService.revokeAllUserTokens(user);

        // Blacklist current access token if present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!redisBlacklistService.isBlacklisted(token)) {
                try {
                    Date expiryDate = jwtService.getExpirationDateFromToken(token);
                    long ttlInMs = expiryDate.getTime() - System.currentTimeMillis();

                    redisBlacklistService.blacklistToken(token, ttlInMs);
                    log.info("Blacklisted current access token for user {}", email);
                } catch (Exception e) {
                    log.warn("Could not extract expiry from token for blacklisting: {}", e.getMessage());
                }
            }
        }

        // Clear security context to force re-authentication
        SecurityContextHolder.clearContext();
        log.info("Password changed successfully for user: {}", email);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Email does not exist"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been deactivated");
        }

        // Generate reset token
        String tokenStr = UUID.randomUUID().toString();
        String redisKey = RESET_PASSWORD_KEY_PREFIX + tokenStr;

        // Save to Redis with 15 minutes expiration
        redisTemplate.opsForValue().set(redisKey, user.getEmail(), 15, TimeUnit.MINUTES);

        // Send Email
        String resetLink = "http://localhost:3000/reset-password?token=" + tokenStr;
        mailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Password reset token generated and email queued for: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String redisKey = RESET_PASSWORD_KEY_PREFIX + request.getToken();
        String email = redisTemplate.opsForValue().get(redisKey);
        if (email == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Token invalid or expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User associated with this token does not exist"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been deactivated");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete key from Redis to mark it as used
        redisTemplate.delete(redisKey);

        // Revoke all refresh tokens for the user
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Password reset successfully using token for user: {}", user.getEmail());
    }
}
