package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.ChangePasswordRequest;
import com.example.finalproject.model.dto.request.ForgotPasswordRequest;
import com.example.finalproject.model.dto.request.ResetPasswordRequest;
import com.example.finalproject.model.entity.PasswordResetToken;
import com.example.finalproject.model.entity.TokenBlacklist;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.PasswordResetTokenRepository;
import com.example.finalproject.repository.TokenBlacklistRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.MailService;
import com.example.finalproject.service.PasswordService;
import com.example.finalproject.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final JwtService jwtService;

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
            if (!tokenBlacklistRepository.existsByToken(token)) {
                try {
                    Date expiryDate = jwtService.getExpirationDateFromToken(token);
                    LocalDateTime expiredAt = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());

                    TokenBlacklist blacklistEntry = TokenBlacklist.builder()
                            .token(token)
                            .expiredAt(expiredAt)
                            .build();

                    tokenBlacklistRepository.save(blacklistEntry);
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

        // Generate reset token
        String tokenStr = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenStr)
                .expiredAt(expiredAt)
                .used(false)
                .user(user)
                .build();

        tokenRepository.save(resetToken);

        // Send Email
        String resetLink = "http://localhost:3000/reset-password?token=" + tokenStr;
        mailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Password reset token generated and email queued for: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Token invalid"));

        if (resetToken.isUsed()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Token already used");
        }

        if (resetToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Revoke all refresh tokens for the user
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Password reset successfully using token for user: {}", user.getEmail());
    }
}
