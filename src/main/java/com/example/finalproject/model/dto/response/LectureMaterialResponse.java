package com.example.finalproject.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureMaterialResponse {
    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private LocalDateTime uploadedAt;
    private String status;
    private Long courseId;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
