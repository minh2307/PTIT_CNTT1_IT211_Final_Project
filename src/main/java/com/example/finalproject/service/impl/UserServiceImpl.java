package com.example.finalproject.service.impl;

import com.example.finalproject.exception.AppException;
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

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(request.getStatus())
                .build();

        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Classroom not found"));
            user.setClassRoom(classRoom);
        }

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByKeywordProjected(keyword, pageable);
        } else {
            return userRepository.findAllProjected(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findDtoById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
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
        return toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .classRoomId(user.getClassRoom() != null ? user.getClassRoom().getId() : null)
                .classCode(user.getClassRoom() != null ? user.getClassRoom().getClassCode() : null)
                .className(user.getClassRoom() != null ? user.getClassRoom().getClassName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
