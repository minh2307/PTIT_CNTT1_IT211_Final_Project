package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.RefreshTokenService;
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
}
