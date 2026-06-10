package com.example.finalproject.controller;

import com.example.finalproject.model.dto.request.SubmissionRequest;
import com.example.finalproject.model.dto.response.SubmissionResponse;
import com.example.finalproject.security.CustomAccessDeniedHandler;
import com.example.finalproject.security.CustomAuthenticationEntryPoint;
import com.example.finalproject.security.jwt.JwtAuthenticationFilter;
import com.example.finalproject.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    // Security mocks to allow context to load
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    void submitProject_Success() throws Exception {
        // Arrange
        Long projectId = 1L;
        SubmissionResponse response = SubmissionResponse.builder()
                .id(10L)
                .title("My Submission")
                .description("Done")
                .githubUrl("https://github.com/user/repo")
                .status("SUBMITTED")
                .build();

        when(submissionService.submitProject(anyLong(), any(SubmissionRequest.class), anyString()))
                .thenReturn(response);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-project.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy zip content".getBytes()
        );

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                "student@gmail.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/student/projects/{projectId}/submit", projectId)
                        .file(mockFile)
                        .param("title", "My Submission")
                        .param("description", "Done")
                        .param("githubUrl", "https://github.com/user/repo")
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Submission created successfully"))
                .andExpect(jsonPath("$.result.id").value(10))
                .andExpect(jsonPath("$.result.title").value("My Submission"))
                .andExpect(jsonPath("$.result.githubUrl").value("https://github.com/user/repo"));

        verify(submissionService, times(1)).submitProject(eq(projectId), any(SubmissionRequest.class), eq("student@gmail.com"));
    }
}
