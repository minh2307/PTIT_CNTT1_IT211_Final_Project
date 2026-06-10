package com.example.finalproject.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {
    private Long id;
    private Double score;
    private String feedback;
    private LocalDateTime gradedAt;
    private String status;
    private Long submissionId;
    private Long graderId;
    private String graderName;
}
