package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
import com.example.finalproject.mapper.UserMapper;
import com.example.finalproject.model.dto.request.UserCreateRequest;
import com.example.finalproject.model.dto.request.UserUpdateRequest;
import com.example.finalproject.model.dto.response.UserResponse;
import com.example.finalproject.model.entity.ClassRoom;
import com.example.finalproject.model.entity.User;
import com.example.finalproject.repository.ClassRoomRepository;
import com.example.finalproject.repository.UserRepository;
import com.example.finalproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));
            user.setClassRoom(classRoom);
        }

        User savedUser = userRepository.save(user);
        return UserMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        return UserMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));
            user.setClassRoom(classRoom);
        } else {
            user.setClassRoom(null);
        }

        User updatedUser = userRepository.save(user);
        return UserMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
