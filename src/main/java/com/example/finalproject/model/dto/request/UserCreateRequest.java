package com.example.finalproject.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Password must be at least 8 characters, contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;

    private String phoneNumber;

    @NotBlank(message = "Role is required")
    private String role; // "STUDENT", "TEACHER", "ADMIN"

    @NotBlank(message = "Status is required")
    private String status; // "ACTIVE", "INACTIVE"

    private Long classRoomId;
}
