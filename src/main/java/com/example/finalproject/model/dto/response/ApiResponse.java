package com.example.finalproject.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
