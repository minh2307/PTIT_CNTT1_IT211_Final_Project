package com.example.finalproject.service;

import com.example.finalproject.model.dto.response.EnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {
    void enrollCourse(Long courseId, String studentEmail);
    Page<EnrollmentResponse> listStudentEnrollments(String studentEmail, Pageable pageable);
}
