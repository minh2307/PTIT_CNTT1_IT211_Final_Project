package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.ChangePasswordRequest;
import com.example.finalproject.model.dto.request.ForgotPasswordRequest;
import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.ResetPasswordRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;

public interface AuthenticationService {
    void register(UserRegisterRequest request);
    LoginResponse login(LoginRequest request);
}
