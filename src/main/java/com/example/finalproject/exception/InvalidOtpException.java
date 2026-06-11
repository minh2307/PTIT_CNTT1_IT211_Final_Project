package com.example.finalproject.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BusinessException {
    public InvalidOtpException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
