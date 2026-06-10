package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.SubmissionRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.SubmissionResponse;
import com.example.finalproject.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentSubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(value = "/projects/{projectId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitProject(
            @PathVariable Long projectId,
            @ModelAttribute @Valid SubmissionRequest request,
            Principal principal) {
        SubmissionResponse responseData = submissionService.submitProject(projectId, request, principal.getName());
        ApiResponse<SubmissionResponse> response = ApiResponse.success(201, "Submission created successfully", responseData);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubmissionResponse> submissions = submissionService.getMySubmissions(principal.getName(), pageable);
        ApiResponse<Page<SubmissionResponse>> response = ApiResponse.success(200, "Fetch submissions success", submissions);
        return ResponseEntity.ok(response);
    }
}
