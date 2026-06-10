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
import com.example.finalproject.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MailService mailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    @Test
    void changePassword_Success() {
        String email = "student@gmail.com";
        String authHeader = "Bearer token123";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass@123")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        User user = User.builder()
                .email(email)
                .password("encodedOldPass")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPass");
        when(tokenBlacklistRepository.existsByToken("token123")).thenReturn(false);
        when(jwtService.getExpirationDateFromToken("token123")).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        assertDoesNotThrow(() -> passwordService.changePassword(request, email, authHeader));

        verify(userRepository, times(1)).save(user);
        verify(refreshTokenService, times(1)).revokeAllUserTokens(user);
        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
    }

    @Test
    void changePassword_UserNotFound_ThrowsAppException() {
        String email = "notfound@gmail.com";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass@123")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> passwordService.changePassword(request, email, null));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void changePassword_OldPasswordIncorrect_ThrowsAppException() {
        String email = "student@gmail.com";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("WrongOldPass")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        User user = User.builder()
                .email(email)
                .password("encodedOldPass")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> passwordService.changePassword(request, email, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Old password incorrect", exception.getMessage());
    }

    @Test
    void changePassword_NewPasswordEqualsOldPassword_ThrowsAppException() {
        String email = "student@gmail.com";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass@123")
                .newPassword("OldPass@123")
                .confirmPassword("OldPass@123")
                .build();

        User user = User.builder()
                .email(email)
                .password("encodedOldPass")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> passwordService.changePassword(request, email, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("New password must be different from old password", exception.getMessage());
    }

    @Test
    void changePassword_ConfirmPasswordMismatch_ThrowsAppException() {
        String email = "student@gmail.com";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass@123")
                .newPassword("NewPass@123")
                .confirmPassword("DifferentConfirmPass")
                .build();

        User user = User.builder()
                .email(email)
                .password("encodedOldPass")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> passwordService.changePassword(request, email, null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Confirm password does not match", exception.getMessage());
    }

    @Test
    void forgotPassword_Success() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("student@gmail.com")
                .build();

        User user = User.builder()
                .email("student@gmail.com")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> passwordService.forgotPassword(request));

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(mailService, times(1)).sendPasswordResetEmail(eq("student@gmail.com"), any(String.class));
    }

    @Test
    void forgotPassword_EmailDoesNotExist_ThrowsAppException() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("nonexistent@gmail.com")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> passwordService.forgotPassword(request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Email does not exist", exception.getMessage());
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("validTokenStr")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        User user = User.builder()
                .email("student@gmail.com")
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("validTokenStr")
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .user(user)
                .build();

        when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPass");

        assertDoesNotThrow(() -> passwordService.resetPassword(request));

        assertTrue(resetToken.isUsed());
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).save(resetToken);
        verify(refreshTokenService, times(1)).revokeAllUserTokens(user);
    }

    @Test
    void resetPassword_TokenNotFound_ThrowsAppException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("invalidTokenStr")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> passwordService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Token invalid", exception.getMessage());
    }

    @Test
    void resetPassword_TokenAlreadyUsed_ThrowsAppException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("usedTokenStr")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("usedTokenStr")
                .used(true)
                .build();

        when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));

        AppException exception = assertThrows(AppException.class, () -> passwordService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Token already used", exception.getMessage());
    }

    @Test
    void resetPassword_TokenExpired_ThrowsAppException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expiredTokenStr")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("expiredTokenStr")
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));

        AppException exception = assertThrows(AppException.class, () -> passwordService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Token expired", exception.getMessage());
    }

    @Test
    void resetPassword_ConfirmPasswordMismatch_ThrowsAppException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("validTokenStr")
                .newPassword("NewPass@123")
                .confirmPassword("DifferentConfirmPass")
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("validTokenStr")
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));

        AppException exception = assertThrows(AppException.class, () -> passwordService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Confirm password does not match", exception.getMessage());
    }
}
