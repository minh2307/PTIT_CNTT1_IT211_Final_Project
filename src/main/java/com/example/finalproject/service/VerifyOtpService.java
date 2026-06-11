package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.VerifyOtpRequest;
import com.example.finalproject.model.dto.response.VerifyOtpResponse;

public interface VerifyOtpService {
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
}
