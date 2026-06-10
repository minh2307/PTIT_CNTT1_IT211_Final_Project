package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.RefreshTokenRequest;
import com.example.finalproject.model.dto.response.RefreshTokenResponse;
import com.example.finalproject.model.entity.RefreshToken;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.RefreshTokenRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        String tokenStr = jwtService.generateRefreshToken(user.getEmail());
        Date expiryDate = jwtService.getExpirationDateFromToken(tokenStr);
        LocalDateTime expiredAt = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .expiredAt(expiredAt)
                .revoked(false)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }

    @Override
    @Transactional
    public RefreshTokenResponse rotateRefreshToken(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();
        RefreshToken dbToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (dbToken.isRevoked()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }

        if (dbToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        if (!jwtService.validateToken(tokenStr)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token signature");
        }

        User user = dbToken.getUser();

        // 1. Revoke old token
        dbToken.setRevoked(true);
        refreshTokenRepository.save(dbToken);

        // 2. Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user.getEmail());
        String newRefreshToken = createRefreshToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTimeSeconds())
                .build();
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);
    }
}
