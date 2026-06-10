package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.LectureMaterialRequest;
import com.example.finalproject.model.dto.response.LectureMaterialResponse;

import java.util.List;

public interface LectureMaterialService {
    LectureMaterialResponse uploadMaterial(Long courseId, LectureMaterialRequest request, String lecturerEmail);
    List<LectureMaterialResponse> getCourseMaterials(Long courseId, String userEmail);
}
