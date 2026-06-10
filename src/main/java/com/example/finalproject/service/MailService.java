package com.example.finalproject.service;

public interface MailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
