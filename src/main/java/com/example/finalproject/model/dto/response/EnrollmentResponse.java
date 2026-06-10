package com.example.finalproject.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long id;
    private LocalDateTime enrolledAt;
    private String status;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long studentId;
    private String studentName;
}
