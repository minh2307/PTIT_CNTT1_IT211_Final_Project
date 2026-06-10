package com.example.finalproject.repository;

import com.example.finalproject.model.entity.LectureMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {
    List<LectureMaterial> findByCourseId(Long courseId);
    List<LectureMaterial> findByCourseIdAndStatus(Long courseId, String status);
}
