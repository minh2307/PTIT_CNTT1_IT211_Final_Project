package com.example.finalproject.mapper;

import com.example.finalproject.model.dto.request.UserCreateRequest;
import com.example.finalproject.model.dto.request.UserRegisterRequest;
import com.example.finalproject.model.dto.response.UserResponse;
import com.example.finalproject.model.entity.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
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

    public static User toUser(UserRegisterRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .role("STUDENT")
                .status("ACTIVE")
                .build();
    }

    public static User toUser(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(request.getStatus())
                .build();
    }
}
