package com.example.finalproject.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final int code;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.code = status.value();
    }

    public AppException(HttpStatus status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
