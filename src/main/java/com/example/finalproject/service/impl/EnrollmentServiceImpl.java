package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.model.dto.response.EnrollmentResponse;
import com.example.finalproject.model.entity.Course;
import com.example.finalproject.model.entity.Enrollment;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.CourseRepository;
import com.example.finalproject.repository.EnrollmentRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void enrollCourse(Long courseId, String studentEmail) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Student not found"));

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Only students can enroll in courses");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!"OPEN".equalsIgnoreCase(course.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Course is not open for enrollment");
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You are already enrolled in this course");
        }

        long activeEnrollmentsCount = enrollmentRepository.countByCourseIdAndStatus(courseId, "ENROLLED");
        if (course.getMaxStudents() != null && activeEnrollmentsCount >= course.getMaxStudents()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Course is full");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status("ENROLLED")
                .build();

        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> listStudentEnrollments(String studentEmail, Pageable pageable) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Student not found"));

        return enrollmentRepository.findDtoByUserId(user.getId(), pageable);
    }
}
