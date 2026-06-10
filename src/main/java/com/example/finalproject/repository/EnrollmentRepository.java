package com.example.finalproject.repository;

import com.example.finalproject.model.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.finalproject.model.dto.response.EnrollmentResponse;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseIdAndStatus(Long courseId, String status);
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.EnrollmentResponse(" +
           "e.id, e.enrolledAt, e.status, c.id, c.courseCode, c.courseName, u.id, u.fullName) " +
           "FROM Enrollment e " +
           "JOIN e.course c " +
           "JOIN e.user u " +
           "WHERE u.id = :userId")
    Page<EnrollmentResponse> findDtoByUserId(@Param("userId") Long userId, Pageable pageable);
}
