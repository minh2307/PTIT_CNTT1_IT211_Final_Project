package com.example.finalproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String githubUrl;

    private MultipartFile file;
}
