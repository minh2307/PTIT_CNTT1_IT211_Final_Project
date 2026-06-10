package com.example.finalproject.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private Long classRoomId;
    private String classCode;
    private String className;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
