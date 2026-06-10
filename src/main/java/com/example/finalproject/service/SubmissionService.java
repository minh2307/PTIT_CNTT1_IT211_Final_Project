package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.SubmissionRequest;
import com.example.finalproject.model.dto.response.SubmissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubmissionService {
    SubmissionResponse submitProject(Long projectId, SubmissionRequest request, String email);
    Page<SubmissionResponse> getMySubmissions(String email, Pageable pageable);
}
