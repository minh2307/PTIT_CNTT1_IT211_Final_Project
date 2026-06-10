package com.example.finalproject.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "class_rooms")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", nullable = false, unique = true)
    private String classCode;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status; // String directly instead of Enum (e.g., "ACTIVE", "INACTIVE")

    @OneToMany(mappedBy = "classRoom", cascade = CascadeType.ALL)
    private List<User> users;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
