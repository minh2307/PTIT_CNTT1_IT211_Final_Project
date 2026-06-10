package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.response.LectureMaterialResponse;
import com.example.finalproject.model.entity.LectureMaterial;
import org.springframework.stereotype.Component;

@Component
public class LectureMaterialMapper {

    public LectureMaterialResponse toLectureMaterialResponse(LectureMaterial material) {
        if (material == null) {
            return null;
        }

        return LectureMaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileUrl(material.getFileUrl())
                .fileType(material.getFileType())
                .uploadedAt(material.getUploadedAt())
                .status(material.getStatus())
                .courseId(material.getCourse() != null ? material.getCourse().getId() : null)
                .courseName(material.getCourse() != null ? material.getCourse().getCourseName() : null)
                .lecturerId(material.getLecturer() != null ? material.getLecturer().getId() : null)
                .lecturerName(material.getLecturer() != null ? material.getLecturer().getFullName() : null)
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }
}
