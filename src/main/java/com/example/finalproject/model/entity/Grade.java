package com.example.finalproject.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "graded_at", nullable = false, updatable = false)
    private LocalDateTime gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grader_id", nullable = false)
    private User grader; // Lecturers/Teachers who grade the submission
}
