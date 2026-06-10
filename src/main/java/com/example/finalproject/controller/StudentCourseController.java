package com.example.finalproject.controller;

import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.EnrollmentResponse;
import com.example.finalproject.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentCourseController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ApiResponse<Void>> enrollCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        String email = authentication.getName();
        enrollmentService.enrollCourse(courseId, email);
        return ResponseEntity.ok(ApiResponse.success(200, "Enroll successfully"));
    }

    @GetMapping("/enrollments")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> listEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<EnrollmentResponse> responses = enrollmentService.listStudentEnrollments(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "Enrollments retrieved successfully", responses));
    }
}
