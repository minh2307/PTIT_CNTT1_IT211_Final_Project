package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.SubmissionMapper;
import com.example.finalproject.model.dto.request.SubmissionRequest;
import com.example.finalproject.model.dto.response.SubmissionResponse;
import com.example.finalproject.model.entity.Project;
import com.example.finalproject.model.entity.Submission;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.ProjectRepository;
import com.example.finalproject.repository.SubmissionRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.service.FileStorageService;
import com.example.finalproject.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SubmissionMapper submissionMapper;

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("^https://(www\\.)?github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_.-]+/?$");

    @Override
    @Transactional
    public SubmissionResponse submitProject(Long projectId, SubmissionRequest request, String email) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Rule: Only STUDENT can submit
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Only students can submit assignments");
        }

        // Rule: Project must exist
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Project not found"));

        // Rule: Submit only before deadline
        if (project.getDeadline() != null && LocalDateTime.now().isAfter(project.getDeadline())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot submit after project deadline: " + project.getDeadline());
        }

        // Validation: Must submit either GitHub URL or a file
        boolean hasGithub = request.getGithubUrl() != null && !request.getGithubUrl().trim().isEmpty();
        boolean hasFile = request.getFile() != null && !request.getFile().isEmpty();

        if (!hasGithub && !hasFile) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Must provide either a GitHub URL or a file attachment");
        }

        // Rule: GitHub URL must be valid
        if (hasGithub) {
            String githubUrl = request.getGithubUrl().trim();
            if (!githubUrl.startsWith("https://github.com/")) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Invalid GitHub URL format. It must start with https://github.com/");
            }
        }

        String fileUrl = null;
        if (hasFile) {
            fileUrl = fileStorageService.storeFile(request.getFile());
        }

        Submission submission = Submission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .githubUrl(hasGithub ? request.getGithubUrl().trim() : null)
                .fileUrl(fileUrl)
                .status("SUBMITTED")
                .user(user)
                .project(project)
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        return submissionMapper.toSubmissionResponse(savedSubmission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getMySubmissions(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        Page<Submission> submissions = submissionRepository.findByUser(user, pageable);
        return submissions.map(submissionMapper::toSubmissionResponse);
    }
}
