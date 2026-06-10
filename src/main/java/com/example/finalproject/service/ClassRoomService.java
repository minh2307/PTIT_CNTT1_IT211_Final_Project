package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.ClassRoomRequest;
import com.example.finalproject.model.dto.response.ClassRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassRoomService {
    ClassRoomResponse createClass(ClassRoomRequest request);
    Page<ClassRoomResponse> listClasses(String keyword, Pageable pageable);
    ClassRoomResponse getClassById(Long id);
    ClassRoomResponse updateClass(Long id, ClassRoomRequest request);
    void deleteClass(Long id);
}
