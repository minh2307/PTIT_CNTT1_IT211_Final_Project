package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.response.SubmissionResponse;
import com.example.finalproject.model.entity.Submission;
import org.springframework.stereotype.Component;

@Component
public class SubmissionMapper {

    public SubmissionResponse toSubmissionResponse(Submission submission) {
        if (submission == null) {
            return null;
        }

        return SubmissionResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .description(submission.getDescription())
                .githubUrl(submission.getGithubUrl())
                .fileUrl(submission.getFileUrl())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .status(submission.getStatus())
                .studentId(submission.getUser() != null ? submission.getUser().getId() : null)
                .studentName(submission.getUser() != null ? submission.getUser().getFullName() : null)
                .projectId(submission.getProject() != null ? submission.getProject().getId() : null)
                .projectName(submission.getProject() != null ? submission.getProject().getProjectName() : null)
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
