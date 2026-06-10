package com.example.finalproject.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer maxStudents;
    private String status;
}
