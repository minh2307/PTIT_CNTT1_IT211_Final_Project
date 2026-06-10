package com.example.finalproject.repository;

import com.example.finalproject.model.entity.ClassRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    boolean existsByClassCode(String classCode);
    Page<ClassRoom> findByClassCodeContainingIgnoreCaseOrClassNameContainingIgnoreCase(String classCode, String className, Pageable pageable);
}
