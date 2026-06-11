package com.example.finalproject.service;

public interface OtpService {
    String generateOtp(String email);
    boolean verifyOtp(String email, String otp);
    void resendOtp(String email);
    void clearOtp(String email);
}
