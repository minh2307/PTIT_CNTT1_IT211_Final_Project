package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.entity.TokenBlacklist;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.TokenBlacklistRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.LogoutService;
import com.example.finalproject.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }

        // Add to blacklist if not already blacklisted
        if (!tokenBlacklistRepository.existsByToken(token)) {
            Date expiryDate = jwtService.getExpirationDateFromToken(token);
            LocalDateTime expiredAt = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());

            TokenBlacklist blacklistEntry = TokenBlacklist.builder()
                    .token(token)
                    .expiredAt(expiredAt)
                    .build();

            tokenBlacklistRepository.save(blacklistEntry);
        }

        // Revoke all refresh tokens for the user
        String email = jwtService.getUsernameFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        refreshTokenService.revokeAllUserTokens(user);

        // Clear security context
        SecurityContextHolder.clearContext();
    }
}
