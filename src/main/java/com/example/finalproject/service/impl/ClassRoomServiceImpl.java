package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.ClassRoomMapper;
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

        ClassRoom classRoom = ClassRoomMapper.toClassRoom(request);
        ClassRoom savedClass = classRoomRepository.save(classRoom);
        return ClassRoomMapper.toClassRoomResponse(savedClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassRoomResponse> listClasses(String keyword, Pageable pageable) {
        Page<ClassRoom> classes;
        if (keyword != null && !keyword.trim().isEmpty()) {
            classes = classRoomRepository.findByClassCodeContainingIgnoreCaseOrClassNameContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            classes = classRoomRepository.findAll(pageable);
        }
        return classes.map(ClassRoomMapper::toClassRoomResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassRoomResponse getClassById(Long id) {
        ClassRoom classRoom = classRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));
        return ClassRoomMapper.toClassRoomResponse(classRoom);
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
        return ClassRoomMapper.toClassRoomResponse(updatedClass);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        if (!classRoomRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Classroom not found");
        }
        classRoomRepository.deleteById(id);
    }
}
