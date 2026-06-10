package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.ChangePasswordRequest;
import com.example.finalproject.model.dto.request.ForgotPasswordRequest;
import com.example.finalproject.model.dto.request.ResetPasswordRequest;

public interface PasswordService {
    void changePassword(ChangePasswordRequest request, String email, String authHeader);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
