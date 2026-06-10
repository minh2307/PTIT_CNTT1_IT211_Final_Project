package com.example.finalproject.repository;

import com.example.finalproject.model.entity.ClassRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.finalproject.model.dto.response.ClassRoomResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    boolean existsByClassCode(String classCode);
    Page<ClassRoom> findByClassCodeContainingIgnoreCaseOrClassNameContainingIgnoreCase(String classCode, String className, Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.ClassRoomResponse(" +
           "c.id, c.classCode, c.className, c.description, c.status, c.createdAt, c.updatedAt) " +
           "FROM ClassRoom c")
    Page<ClassRoomResponse> findAllProjected(Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.ClassRoomResponse(" +
           "c.id, c.classCode, c.className, c.description, c.status, c.createdAt, c.updatedAt) " +
           "FROM ClassRoom c " +
           "WHERE LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ClassRoomResponse> findByKeywordProjected(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.ClassRoomResponse(" +
           "c.id, c.classCode, c.className, c.description, c.status, c.createdAt, c.updatedAt) " +
           "FROM ClassRoom c " +
           "WHERE c.id = :id")
    Optional<ClassRoomResponse> findDtoById(@Param("id") Long id);
}
