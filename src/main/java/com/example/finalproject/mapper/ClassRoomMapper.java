package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.request.ClassRoomRequest;
import com.example.finalproject.model.dto.response.ClassRoomResponse;
import com.example.finalproject.model.entity.ClassRoom;

public class ClassRoomMapper {

    public static ClassRoomResponse toClassRoomResponse(ClassRoom classroom) {
        if (classroom == null) {
            return null;
        }
        return ClassRoomResponse.builder()
                .id(classroom.getId())
                .classCode(classroom.getClassCode())
                .className(classroom.getClassName())
                .description(classroom.getDescription())
                .status(classroom.getStatus())
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .build();
    }

    public static ClassRoom toClassRoom(ClassRoomRequest request) {
        if (request == null) {
            return null;
        }
        return ClassRoom.builder()
                .classCode(request.getClassCode())
                .className(request.getClassName())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
    }
}
