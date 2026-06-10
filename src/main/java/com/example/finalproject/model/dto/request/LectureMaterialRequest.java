package com.example.finalproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureMaterialRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private MultipartFile file;
}
