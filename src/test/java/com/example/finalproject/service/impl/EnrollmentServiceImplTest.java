package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.entity.Course;
import com.example.finalproject.model.entity.Enrollment;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.CourseRepository;
import com.example.finalproject.repository.EnrollmentRepository;
import com.example.finalproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void enrollCourse_Success() {
        String email = "student@gmail.com";
        Long courseId = 1L;

        User user = User.builder().id(2L).email(email).role("STUDENT").build();
        Course course = Course.builder().id(courseId).status("OPEN").maxStudents(30).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(courseId, "ENROLLED")).thenReturn(15L);

        assertDoesNotThrow(() -> enrollmentService.enrollCourse(courseId, email));

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_NotAStudent_ThrowsAppException() {
        String email = "teacher@gmail.com";
        Long courseId = 1L;

        User user = User.builder().id(2L).email(email).role("TEACHER").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> enrollmentService.enrollCourse(courseId, email));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only students can enroll in courses", exception.getMessage());

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_CourseNotOpen_ThrowsAppException() {
        String email = "student@gmail.com";
        Long courseId = 1L;

        User user = User.builder().id(2L).email(email).role("STUDENT").build();
        Course course = Course.builder().id(courseId).status("CLOSED").maxStudents(30).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        AppException exception = assertThrows(AppException.class, () -> enrollmentService.enrollCourse(courseId, email));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Course is not open for enrollment", exception.getMessage());

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_DuplicateEnrollment_ThrowsAppException() {
        String email = "student@gmail.com";
        Long courseId = 1L;

        User user = User.builder().id(2L).email(email).role("STUDENT").build();
        Course course = Course.builder().id(courseId).status("OPEN").maxStudents(30).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> enrollmentService.enrollCourse(courseId, email));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("You are already enrolled in this course", exception.getMessage());

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_CourseFull_ThrowsAppException() {
        String email = "student@gmail.com";
        Long courseId = 1L;

        User user = User.builder().id(2L).email(email).role("STUDENT").build();
        Course course = Course.builder().id(courseId).status("OPEN").maxStudents(20).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(courseId, "ENROLLED")).thenReturn(20L);

        AppException exception = assertThrows(AppException.class, () -> enrollmentService.enrollCourse(courseId, email));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Course is full", exception.getMessage());

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }
}
