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
import com.example.finalproject.service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private SubmissionMapper submissionMapper;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Test
    void submitProject_Success() {
        // Arrange
        String email = "student@gmail.com";
        Long projectId = 1L;

        User user = User.builder()
                .id(2L)
                .email(email)
                .role("STUDENT")
                .build();

        Project project = Project.builder()
                .id(projectId)
                .projectName("Final Project")
                .deadline(LocalDateTime.now().plusDays(2))
                .build();

        SubmissionRequest request = SubmissionRequest.builder()
                .title("My Submission")
                .description("Finished project")
                .githubUrl("https://github.com/user/repo")
                .build();

        Submission submission = Submission.builder()
                .id(10L)
                .title(request.getTitle())
                .description(request.getDescription())
                .githubUrl(request.getGithubUrl())
                .status("SUBMITTED")
                .user(user)
                .project(project)
                .build();

        SubmissionResponse expectedResponse = SubmissionResponse.builder()
                .id(10L)
                .title(request.getTitle())
                .description(request.getDescription())
                .githubUrl(request.getGithubUrl())
                .status("SUBMITTED")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        when(submissionMapper.toSubmissionResponse(any(Submission.class))).thenReturn(expectedResponse);

        // Act
        SubmissionResponse response = submissionService.submitProject(projectId, request, email);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(expectedResponse.getTitle(), response.getTitle());
        assertEquals(expectedResponse.getGithubUrl(), response.getGithubUrl());
        assertEquals("SUBMITTED", response.getStatus());

        verify(userRepository, times(1)).findByEmail(email);
        verify(projectRepository, times(1)).findById(projectId);
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }
}
