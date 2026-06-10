package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.GradeRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.GradeResponse;
import com.example.finalproject.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerGradeController {

    private final GradeService gradeService;

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse<GradeResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeRequest request,
            Principal principal) {
        GradeResponse gradeResponse = gradeService.gradeSubmission(submissionId, request, principal.getName());
        ApiResponse<GradeResponse> response = ApiResponse.success(200, "Graded successfully", gradeResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<Page<GradeResponse>>> getGradedSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GradeResponse> grades = gradeService.getGradedSubmissions(principal.getName(), pageable);
        ApiResponse<Page<GradeResponse>> response = ApiResponse.success(200, "Fetch grades success", grades);
        return ResponseEntity.ok(response);
    }
}
