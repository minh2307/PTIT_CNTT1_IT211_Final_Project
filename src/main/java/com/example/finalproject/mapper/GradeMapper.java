package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.response.GradeResponse;
import com.example.finalproject.model.entity.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeResponse toGradeResponse(Grade grade) {
        if (grade == null) {
            return null;
        }

        return GradeResponse.builder()
                .id(grade.getId())
                .score(grade.getScore())
                .feedback(grade.getFeedback())
                .gradedAt(grade.getGradedAt())
                .status(grade.getStatus())
                .submissionId(grade.getSubmission() != null ? grade.getSubmission().getId() : null)
                .graderId(grade.getGrader() != null ? grade.getGrader().getId() : null)
                .graderName(grade.getGrader() != null ? grade.getGrader().getFullName() : null)
                .build();
    }
}
