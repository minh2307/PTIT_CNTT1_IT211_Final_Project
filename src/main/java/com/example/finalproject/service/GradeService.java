package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.GradeRequest;
import com.example.finalproject.model.dto.response.GradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GradeService {
    GradeResponse gradeSubmission(Long submissionId, GradeRequest request, String lecturerEmail);
    Page<GradeResponse> getGradedSubmissions(String lecturerEmail, Pageable pageable);
}
