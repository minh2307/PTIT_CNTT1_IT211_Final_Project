package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.VerifyOtpRequest;
import com.example.finalproject.model.dto.response.VerifyOtpResponse;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtService;
import com.example.finalproject.service.OtpService;
import com.example.finalproject.service.RefreshTokenService;
import com.example.finalproject.service.VerifyOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyOtpServiceImpl implements VerifyOtpService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        // First check if user exists in database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Email does not exist"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been deactivated");
        }

        if (!"LECTURER".equalsIgnoreCase(user.getRole())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Two-factor authentication is not enabled for this account");
        }

        // Verify OTP (will throw exception if expired/invalid/exceeded)
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return VerifyOtpResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTimeSeconds())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
