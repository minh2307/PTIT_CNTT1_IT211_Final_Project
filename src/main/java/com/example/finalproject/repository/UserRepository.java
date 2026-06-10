package com.example.finalproject.repository;

import com.example.finalproject.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.example.finalproject.model.dto.response.UserResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email, Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.UserResponse(" +
           "u.id, u.fullName, u.email, u.phoneNumber, u.role, u.status, c.id, c.classCode, c.className, u.createdAt, u.updatedAt) " +
           "FROM User u LEFT JOIN u.classRoom c")
    Page<UserResponse> findAllProjected(Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.UserResponse(" +
           "u.id, u.fullName, u.email, u.phoneNumber, u.role, u.status, c.id, c.classCode, c.className, u.createdAt, u.updatedAt) " +
           "FROM User u LEFT JOIN u.classRoom c " +
           "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserResponse> findByKeywordProjected(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.example.finalproject.model.dto.response.UserResponse(" +
           "u.id, u.fullName, u.email, u.phoneNumber, u.role, u.status, c.id, c.classCode, c.className, u.createdAt, u.updatedAt) " +
           "FROM User u LEFT JOIN u.classRoom c " +
           "WHERE u.id = :id")
    Optional<UserResponse> findDtoById(@Param("id") Long id);
}
