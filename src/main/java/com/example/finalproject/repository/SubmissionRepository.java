package com.example.finalproject.repository;

import com.example.finalproject.model.entity.Submission;
import com.example.finalproject.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByUser(User user, Pageable pageable);
    
    // Find the latest submission for a student on a specific project
    Optional<Submission> findFirstByUserIdAndProjectIdOrderBySubmittedAtDesc(Long userId, Long projectId);
}
