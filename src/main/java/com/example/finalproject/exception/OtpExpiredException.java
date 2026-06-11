package com.example.finalproject.exception;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BusinessException {
    public OtpExpiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
