package com.example.finalproject.service;

public interface LoginAttemptService {
    boolean isLocked(String email);
    void failAttempt(String email);
    void resetAttempts(String email);
}
