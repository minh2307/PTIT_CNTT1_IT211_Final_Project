package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.RefreshTokenService;
import com.example.finalproject.service.OtpService;
import com.example.finalproject.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void register_Success() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("Nguyen Van A")
                .email("a@gmail.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .phoneNumber("0123456789")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> authenticationService.register(request));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_MismatchedPassword_ThrowsAppException() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("Nguyen Van A")
                .email("a@gmail.com")
                .password("Password@123")
                .confirmPassword("Different@123")
                .build();

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Confirm password does not match password", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsAppException() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("Nguyen Van A")
                .email("a@gmail.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Email is already registered", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("a@gmail.com", "Password@123");
        User user = User.builder()
                .email("a@gmail.com")
                .password("encodedPassword")
                .status("ACTIVE")
                .role("STUDENT")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refreshToken");
        when(jwtService.getExpirationTimeSeconds()).thenReturn(3600L);

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("STUDENT", response.getRole());
        assertEquals("a@gmail.com", response.getEmail());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    void login_InvalidCredentials_ThrowsAppException() {
        LoginRequest request = new LoginRequest("a@gmail.com", "wrongPassword");
        User user = User.builder()
                .email("a@gmail.com")
                .password("encodedPassword")
                .status("ACTIVE")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_InactiveAccount_ThrowsAppException() {
        LoginRequest request = new LoginRequest("a@gmail.com", "Password@123");
        User user = User.builder()
                .email("a@gmail.com")
                .password("encodedPassword")
                .status("INACTIVE")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Your account has been deactivated", exception.getMessage());
    }

    @Test
    void login_Lecturer_RequiresOtp() {
        LoginRequest request = new LoginRequest("lecturer@gmail.com", "Password@123");
        User user = User.builder()
                .email("lecturer@gmail.com")
                .password("encodedPassword")
                .status("ACTIVE")
                .role("LECTURER")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(otpService.generateOtp(user.getEmail())).thenReturn("123456");

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("OTP_REQUIRED", response.getStatus());
        assertEquals("OTP has been sent to your email", response.getMessage());
        assertEquals("lecturer@gmail.com", response.getEmail());
        assertEquals("LECTURER", response.getRole());
        assertNull(response.getAccessToken());

        verify(otpService, times(1)).generateOtp(user.getEmail());
        verify(emailService, times(1)).sendOtpEmail(user.getEmail(), "123456");
    }

    @Test
    void resendOtp_Success() {
        com.example.finalproject.model.dto.request.ResendOtpRequest request = 
                new com.example.finalproject.model.dto.request.ResendOtpRequest("lecturer@gmail.com");
        User user = User.builder()
                .email("lecturer@gmail.com")
                .status("ACTIVE")
                .role("LECTURER")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authenticationService.resendOtp(request));

        verify(otpService, times(1)).resendOtp(request.getEmail());
    }

    @Test
    void resendOtp_EmailDoesNotExist_ThrowsAppException() {
        com.example.finalproject.model.dto.request.ResendOtpRequest request = 
                new com.example.finalproject.model.dto.request.ResendOtpRequest("nonexistent@gmail.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> authenticationService.resendOtp(request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Email does not exist", exception.getMessage());
    }
}
