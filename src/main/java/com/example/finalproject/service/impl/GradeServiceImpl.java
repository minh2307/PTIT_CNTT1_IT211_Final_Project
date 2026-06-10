package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.GradeMapper;
import com.example.finalproject.model.dto.request.GradeRequest;
import com.example.finalproject.model.dto.response.GradeResponse;
import com.example.finalproject.model.entity.Grade;
import com.example.finalproject.model.entity.Submission;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.GradeRepository;
import com.example.finalproject.repository.SubmissionRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public GradeResponse gradeSubmission(Long submissionId, GradeRequest request, String lecturerEmail) {
        // Find lecturer
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Rule: Only LECTURER can grade
        if (!"LECTURER".equalsIgnoreCase(lecturer.getRole())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Only lecturers can grade submissions");
        }

        // Rule: Score from 0 to 100
        double score = request.getScore();
        if (score < 0 || score > 100) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Score must be between 0 and 100");
        }

        // Find submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Submission not found"));

        // Rule: One submission has only one final grade.
        // Check if grade already exists for this submission.
        Grade grade = gradeRepository.findBySubmissionId(submissionId).orElse(null);
        if (grade == null) {
            grade = Grade.builder()
                    .submission(submission)
                    .grader(lecturer)
                    .build();
        }

        grade.setScore(score);
        grade.setFeedback(request.getFeedback());
        grade.setGradedAt(LocalDateTime.now());
        grade.setStatus("GRADED");

        Grade savedGrade = gradeRepository.save(grade);

        // Keep submission score & status synchronized
        submission.setScore(score);
        submission.setStatus("GRADED");
        submissionRepository.save(submission);

        return gradeMapper.toGradeResponse(savedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeResponse> getGradedSubmissions(String lecturerEmail, Pageable pageable) {
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Only LECTURER can view their graded submissions
        if (!"LECTURER".equalsIgnoreCase(lecturer.getRole())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Only lecturers can access this data");
        }

        Page<Grade> grades = gradeRepository.findByGrader(lecturer, pageable);
        return grades.map(gradeMapper::toGradeResponse);
    }
}
