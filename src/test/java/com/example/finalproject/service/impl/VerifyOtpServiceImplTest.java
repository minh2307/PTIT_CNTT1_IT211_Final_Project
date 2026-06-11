package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.VerifyOtpRequest;
import com.example.finalproject.model.dto.response.VerifyOtpResponse;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.OtpService;
import com.example.finalproject.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerifyOtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private VerifyOtpServiceImpl verifyOtpService;

    @Test
    void verifyOtp_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest("lecturer@gmail.com", "123456");
        User user = User.builder()
                .email("lecturer@gmail.com")
                .status("ACTIVE")
                .role("LECTURER")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(otpService.verifyOtp(request.getEmail(), request.getOtp())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refreshToken");
        when(jwtService.getExpirationTimeSeconds()).thenReturn(3600L);

        VerifyOtpResponse response = verifyOtpService.verifyOtp(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("lecturer@gmail.com", response.getEmail());
        assertEquals("LECTURER", response.getRole());
        assertEquals(3600L, response.getExpiresIn());

        verify(otpService, times(1)).verifyOtp(request.getEmail(), request.getOtp());
    }

    @Test
    void verifyOtp_UserNotFound_ThrowsAppException() {
        VerifyOtpRequest request = new VerifyOtpRequest("nonexistent@gmail.com", "123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> verifyOtpService.verifyOtp(request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Email does not exist", exception.getMessage());
    }

    @Test
    void verifyOtp_InactiveUser_ThrowsAppException() {
        VerifyOtpRequest request = new VerifyOtpRequest("lecturer@gmail.com", "123456");
        User user = User.builder()
                .email("lecturer@gmail.com")
                .status("INACTIVE")
                .role("LECTURER")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> verifyOtpService.verifyOtp(request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Your account has been deactivated", exception.getMessage());
    }

    @Test
    void verifyOtp_NotLecturer_ThrowsAppException() {
        VerifyOtpRequest request = new VerifyOtpRequest("student@gmail.com", "123456");
        User user = User.builder()
                .email("student@gmail.com")
                .status("ACTIVE")
                .role("STUDENT")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> verifyOtpService.verifyOtp(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Two-factor authentication is not enabled for this account", exception.getMessage());
    }
}
