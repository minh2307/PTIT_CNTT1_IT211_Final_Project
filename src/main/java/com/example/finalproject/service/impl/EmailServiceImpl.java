package com.example.finalproject.service.impl;

import com.example.finalproject.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your One-Time Password (OTP) - Course Management & Project Grading System");
            message.setText("Hello,\n\n"
                    + "Your One-Time Password (OTP) for login is: " + otp + "\n\n"
                    + "This OTP is valid for 120 seconds.\n"
                    + "For security, do not share this code with anyone.\n\n"
                    + "Best regards,\n"
                    + "Course Management & Project Grading System");
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Could not send OTP email. Please try again later.", e);
        }
    }
}
