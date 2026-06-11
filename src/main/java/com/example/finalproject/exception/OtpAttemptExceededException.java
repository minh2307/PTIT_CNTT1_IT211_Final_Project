package com.example.finalproject.exception;

import org.springframework.http.HttpStatus;

public class OtpAttemptExceededException extends BusinessException {
    public OtpAttemptExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
