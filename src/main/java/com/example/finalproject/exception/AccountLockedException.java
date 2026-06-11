package com.example.finalproject.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends AppException {
    public AccountLockedException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
