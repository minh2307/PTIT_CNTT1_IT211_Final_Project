package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.ClassRoomRequest;
import com.example.finalproject.model.dto.response.ApiResponse;
import com.example.finalproject.model.dto.response.ClassRoomResponse;
import com.example.finalproject.service.ClassRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassController {

    private final ClassRoomService classRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassRoomResponse>> createClass(@Valid @RequestBody ClassRoomRequest request) {
        ClassRoomResponse response = classRoomService.createClass(request);
        ApiResponse<ClassRoomResponse> apiResponse = ApiResponse.success(201, "Classroom created successfully", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClassRoomResponse>>> listClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClassRoomResponse> classroomResponses = classRoomService.listClasses(keyword, pageable);
        ApiResponse<Page<ClassRoomResponse>> apiResponse = ApiResponse.success(200, "Classrooms retrieved successfully", classroomResponses);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassRoomResponse>> getClassById(@PathVariable Long id) {
        ClassRoomResponse response = classRoomService.getClassById(id);
        ApiResponse<ClassRoomResponse> apiResponse = ApiResponse.success(200, "Classroom detail retrieved", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassRoomResponse>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassRoomRequest request) {
        ClassRoomResponse response = classRoomService.updateClass(id, request);
        ApiResponse<ClassRoomResponse> apiResponse = ApiResponse.success(200, "Classroom updated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        classRoomService.deleteClass(id);
        ApiResponse<Void> apiResponse = ApiResponse.success(200, "Classroom deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }
}
