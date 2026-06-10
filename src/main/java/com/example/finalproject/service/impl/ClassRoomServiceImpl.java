package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.request.ClassRoomRequest;
import com.example.finalproject.model.dto.response.ClassRoomResponse;
import com.example.finalproject.model.entity.ClassRoom;
import com.example.finalproject.repository.ClassRoomRepository;
import com.example.finalproject.service.ClassRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassRoomServiceImpl implements ClassRoomService {

    private final ClassRoomRepository classRoomRepository;

    @Override
    @Transactional
    public ClassRoomResponse createClass(ClassRoomRequest request) {
        if (classRoomRepository.existsByClassCode(request.getClassCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Class code is already registered");
        }

        ClassRoom classRoom = ClassRoom.builder()
                .classCode(request.getClassCode())
                .className(request.getClassName())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();

        ClassRoom savedClass = classRoomRepository.save(classRoom);
        return toClassRoomResponse(savedClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassRoomResponse> listClasses(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return classRoomRepository.findByKeywordProjected(keyword, pageable);
        } else {
            return classRoomRepository.findAllProjected(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClassRoomResponse getClassById(Long id) {
        return classRoomRepository.findDtoById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));
    }

    @Override
    @Transactional
    public ClassRoomResponse updateClass(Long id, ClassRoomRequest request) {
        ClassRoom classRoom = classRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));

        if (!classRoom.getClassCode().equalsIgnoreCase(request.getClassCode()) && classRoomRepository.existsByClassCode(request.getClassCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Class code is already registered");
        }

        classRoom.setClassCode(request.getClassCode());
        classRoom.setClassName(request.getClassName());
        classRoom.setDescription(request.getDescription());
        classRoom.setStatus(request.getStatus());

        ClassRoom updatedClass = classRoomRepository.save(classRoom);
        return toClassRoomResponse(updatedClass);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        if (!classRoomRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Classroom not found");
        }
        classRoomRepository.deleteById(id);
    }

    private ClassRoomResponse toClassRoomResponse(ClassRoom classRoom) {
        if (classRoom == null) {
            return null;
        }
        return ClassRoomResponse.builder()
                .id(classRoom.getId())
                .classCode(classRoom.getClassCode())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .status(classRoom.getStatus())
                .createdAt(classRoom.getCreatedAt())
                .updatedAt(classRoom.getUpdatedAt())
                .build();
    }
}
