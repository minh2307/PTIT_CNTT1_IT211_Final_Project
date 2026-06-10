package com.example.finalproject.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private String title;
    private String description;
    private String githubUrl;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Double score;
    private String status;
    private Long studentId;
    private String studentName;
    private Long projectId;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
