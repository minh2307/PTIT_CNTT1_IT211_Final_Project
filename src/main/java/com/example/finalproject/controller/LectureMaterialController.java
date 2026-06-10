package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.LectureMaterialRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.LectureMaterialResponse;
import com.example.finalproject.service.LectureMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LectureMaterialController {

    private final LectureMaterialService lectureMaterialService;

    @PostMapping(value = "/api/v1/lecturer/courses/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> uploadMaterial(
            @PathVariable Long courseId,
            @ModelAttribute @Valid LectureMaterialRequest request,
            Principal principal) {
        LectureMaterialResponse responseData = lectureMaterialService.uploadMaterial(courseId, request, principal.getName());
        ApiResponse<LectureMaterialResponse> response = ApiResponse.success(201, "Lecture material uploaded successfully", responseData);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/courses/{courseId}/materials")
    public ResponseEntity<ApiResponse<List<LectureMaterialResponse>>> getCourseMaterials(
            @PathVariable Long courseId,
            Principal principal) {
        List<LectureMaterialResponse> materials = lectureMaterialService.getCourseMaterials(courseId, principal.getName());
        ApiResponse<List<LectureMaterialResponse>> response = ApiResponse.success(200, "Fetch materials success", materials);
        return ResponseEntity.ok(response);
    }
}
