package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.UserCreateRequest;
import com.example.finalproject.model.dto.request.UserUpdateRequest;
import com.example.finalproject.model.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    Page<UserResponse> listUsers(String keyword, Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
