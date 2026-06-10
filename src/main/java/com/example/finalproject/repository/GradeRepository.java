package com.example.finalproject.repository;

import com.example.finalproject.model.entity.Grade;
import com.example.finalproject.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findBySubmissionId(Long submissionId);
    Page<Grade> findByGrader(User grader, Pageable pageable);
}
