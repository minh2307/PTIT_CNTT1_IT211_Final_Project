package com.example.finalproject.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String password; // optional, only update if provided

    private String phoneNumber;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Status is required")
    private String status;

    private Long classRoomId;
}
