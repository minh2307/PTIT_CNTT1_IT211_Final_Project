package com.example.finalproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRoomRequest {

    @NotBlank(message = "Class code is required")
    private String classCode;

    @NotBlank(message = "Class name is required")
    private String className;

    private String description;

    @NotBlank(message = "Status is required")
    private String status; // "ACTIVE", "INACTIVE"
}
