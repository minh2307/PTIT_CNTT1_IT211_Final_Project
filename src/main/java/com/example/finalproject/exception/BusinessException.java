package com.example.finalproject.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {
    public BusinessException(HttpStatus status, String message) {
        super(status, message);
    }
}
