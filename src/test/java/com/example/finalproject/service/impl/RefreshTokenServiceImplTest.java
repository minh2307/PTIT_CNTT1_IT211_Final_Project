package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.RefreshTokenRequest;
import com.example.finalproject.model.dto.response.RefreshTokenResponse;
import com.example.finalproject.model.entity.RefreshToken;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.RefreshTokenRepository;
import com.example.finalproject.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void createRefreshToken_Success() {
        User user = User.builder().email("test@gmail.com").build();
        String tokenStr = "mockRefreshToken";
        Date expiryDate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);

        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn(tokenStr);
        when(jwtService.getExpirationDateFromToken(tokenStr)).thenReturn(expiryDate);

        String result = refreshTokenService.createRefreshToken(user);

        assertEquals(tokenStr, result);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void rotateRefreshToken_Success() {
        User user = User.builder().email("test@gmail.com").build();
        String oldTokenStr = "oldRefreshToken";
        RefreshToken oldTokenEntity = RefreshToken.builder()
                .token(oldTokenStr)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .user(user)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest(oldTokenStr);

        when(refreshTokenRepository.findByToken(oldTokenStr)).thenReturn(Optional.of(oldTokenEntity));
        when(jwtService.validateToken(oldTokenStr)).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("newRefreshToken");
        when(jwtService.getExpirationDateFromToken("newRefreshToken")).thenReturn(new Date());
        when(jwtService.getExpirationTimeSeconds()).thenReturn(3600L);

        RefreshTokenResponse response = refreshTokenService.rotateRefreshToken(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());
        assertTrue(oldTokenEntity.isRevoked());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // 1 for old, 1 for new
    }

    @Test
    void rotateRefreshToken_Revoked_ThrowsAppException() {
        RefreshToken oldTokenEntity = RefreshToken.builder()
                .token("revokedToken")
                .expiredAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest("revokedToken");
        when(refreshTokenRepository.findByToken("revokedToken")).thenReturn(Optional.of(oldTokenEntity));

        AppException exception = assertThrows(AppException.class, () -> refreshTokenService.rotateRefreshToken(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Refresh token has been revoked", exception.getMessage());
    }

    @Test
    void rotateRefreshToken_Expired_ThrowsAppException() {
        RefreshToken oldTokenEntity = RefreshToken.builder()
                .token("expiredToken")
                .expiredAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest("expiredToken");
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(oldTokenEntity));

        AppException exception = assertThrows(AppException.class, () -> refreshTokenService.rotateRefreshToken(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Refresh token has expired", exception.getMessage());
    }

    @Test
    void rotateRefreshToken_InvalidSignature_ThrowsAppException() {
        RefreshToken oldTokenEntity = RefreshToken.builder()
                .token("invalidToken")
                .expiredAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest("invalidToken");
        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.of(oldTokenEntity));
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> refreshTokenService.rotateRefreshToken(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid refresh token signature", exception.getMessage());
    }

    @Test
    void revokeAllUserTokens_Success() {
        User user = User.builder().email("test@gmail.com").build();
        RefreshToken token1 = RefreshToken.builder().revoked(false).build();
        RefreshToken token2 = RefreshToken.builder().revoked(false).build();

        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(List.of(token1, token2));

        refreshTokenService.revokeAllUserTokens(user);

        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
        verify(refreshTokenRepository, times(1)).saveAll(anyList());
    }
}
