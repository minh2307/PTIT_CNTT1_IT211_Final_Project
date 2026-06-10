package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.entity.TokenBlacklist;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.TokenBlacklistRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceImplTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_Success() {
        String authHeader = "Bearer validAccessToken";
        User user = User.builder().email("test@gmail.com").build();
        Date expiryDate = new Date(System.currentTimeMillis() + 15 * 60 * 1000L);

        when(jwtService.validateToken("validAccessToken")).thenReturn(true);
        when(tokenBlacklistRepository.existsByToken("validAccessToken")).thenReturn(false);
        when(jwtService.getExpirationDateFromToken("validAccessToken")).thenReturn(expiryDate);
        when(jwtService.getUsernameFromToken("validAccessToken")).thenReturn("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> logoutService.logout(authHeader));

        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
        verify(refreshTokenService, times(1)).revokeAllUserTokens(user);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_InvalidHeader_ThrowsAppException() {
        AppException exceptionNull = assertThrows(AppException.class, () -> logoutService.logout(null));
        assertEquals(HttpStatus.UNAUTHORIZED, exceptionNull.getStatus());
        assertEquals("Invalid authorization header", exceptionNull.getMessage());

        AppException exceptionEmpty = assertThrows(AppException.class, () -> logoutService.logout(""));
        assertEquals(HttpStatus.UNAUTHORIZED, exceptionEmpty.getStatus());

        AppException exceptionNotBearer = assertThrows(AppException.class, () -> logoutService.logout("Basic xxx"));
        assertEquals(HttpStatus.UNAUTHORIZED, exceptionNotBearer.getStatus());
    }

    @Test
    void logout_InvalidToken_ThrowsAppException() {
        String authHeader = "Bearer invalidToken";
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> logoutService.logout(authHeader));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid access token", exception.getMessage());
    }
}
