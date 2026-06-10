package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;
import com.example.finalproject.security.CustomAccessDeniedHandler;
import com.example.finalproject.security.CustomAuthenticationEntryPoint;
import com.example.finalproject.security.jwt.JwtAuthenticationFilter;
import com.example.finalproject.service.AuthenticationService;
import com.example.finalproject.service.LogoutService;
import com.example.finalproject.service.PasswordService;
import com.example.finalproject.service.RefreshTokenService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private LogoutService logoutService;

    @MockitoBean
    private PasswordService passwordService;

    // Mocks for Spring Security config compatibility in @WebMvcTest
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("student@gmail.com", "Password@123");
        LoginResponse response = LoginResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .tokenType("Bearer")
                .email("student@gmail.com")
                .role("STUDENT")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login success"))
                .andExpect(jsonPath("$.result.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.result.role").value("STUDENT"));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_Failure_Unauthorized() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("student@gmail.com", "wrongPassword");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.example.finalproject.exception.AppException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("Nguyen Van A")
                .email("student@gmail.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .phoneNumber("0123456789")
                .build();

        doNothing().when(authenticationService).register(any(UserRegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Register success"));

        verify(authenticationService, times(1)).register(any(UserRegisterRequest.class));
    }
}
