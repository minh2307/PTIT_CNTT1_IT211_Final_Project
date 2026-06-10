package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.LoginRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.LoginResponse;

public interface AuthService {
    void register(UserRegisterRequest request);
    LoginResponse login(LoginRequest request);
}
