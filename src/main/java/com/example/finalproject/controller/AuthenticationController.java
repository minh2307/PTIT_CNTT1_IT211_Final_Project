package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.RefreshTokenRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.LoginResponse;
import com.example.finalproject.model.dto.response.RefreshTokenResponse;
import com.example.finalproject.service.AuthenticationService;
import com.example.finalproject.service.LogoutService;
import com.example.finalproject.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterRequest request) {
        authenticationService.register(request);
        ApiResponse<Void> response = ApiResponse.success(201, "Register success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(200, "Login success", loginResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse tokenResponse = refreshTokenService.rotateRefreshToken(request);
        ApiResponse<RefreshTokenResponse> response = ApiResponse.success(200, "Token refreshed successfully", tokenResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        logoutService.logout(authHeader);
        ApiResponse<Void> response = ApiResponse.success(200, "Logout successfully");
        return ResponseEntity.ok(response);
    }
}
