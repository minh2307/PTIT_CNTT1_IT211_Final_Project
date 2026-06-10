package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.response.EnrollmentResponse;
import com.example.finalproject.model.entity.Enrollment;

public class EnrollmentMapper {

    public static EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus())
                .courseId(enrollment.getCourse() != null ? enrollment.getCourse().getId() : null)
                .courseCode(enrollment.getCourse() != null ? enrollment.getCourse().getCourseCode() : null)
                .courseName(enrollment.getCourse() != null ? enrollment.getCourse().getCourseName() : null)
                .studentId(enrollment.getUser() != null ? enrollment.getUser().getId() : null)
                .studentName(enrollment.getUser() != null ? enrollment.getUser().getFullName() : null)
                .build();
    }
}
