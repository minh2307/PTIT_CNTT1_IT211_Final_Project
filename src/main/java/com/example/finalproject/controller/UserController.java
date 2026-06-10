package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.ChangePasswordRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final PasswordService passwordService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Principal principal) {
        
        passwordService.changePassword(request, principal.getName(), authHeader);
        ApiResponse<Void> response = ApiResponse.success(200, "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
