package com.example.finalproject.controller;

import com.example.finalproject.security.CustomAccessDeniedHandler;
import com.example.finalproject.security.CustomAuthenticationEntryPoint;
import com.example.finalproject.security.jwt.JwtAuthenticationFilter;
import com.example.finalproject.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentCourseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService enrollmentService;

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
    void enrollCourse_Success() throws Exception {
        // Arrange
        Long courseId = 1L;
        doNothing().when(enrollmentService).enrollCourse(anyLong(), anyString());

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                "student@gmail.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/student/courses/{courseId}/enroll", courseId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Enroll successfully"));

        verify(enrollmentService, times(1)).enrollCourse(eq(courseId), eq("student@gmail.com"));
    }
}
