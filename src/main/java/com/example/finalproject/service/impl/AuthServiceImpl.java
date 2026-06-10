package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.UserMapper;
import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.security.jwt.JwtUtils;
import com.example.finalproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void register(UserRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Confirm password does not match password");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Your account has been deactivated");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateToken(user.getEmail(), 7 * 24 * 60 * 60 * 1000L); // 7 days expiration

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
