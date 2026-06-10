package com.example.finalproject.repository;

import com.example.finalproject.model.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByCourseIdAndStatus(Long courseId, String status);
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);
}
