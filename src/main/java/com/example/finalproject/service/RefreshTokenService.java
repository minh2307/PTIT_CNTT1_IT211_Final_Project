package com.example.finalproject.service;

import com.example.finalproject.model.dto.request.RefreshTokenRequest;
import com.example.finalproject.model.dto.response.RefreshTokenResponse;
import com.example.finalproject.model.entity.User;

public interface RefreshTokenService {
    String createRefreshToken(User user);
    RefreshTokenResponse rotateRefreshToken(RefreshTokenRequest request);
    void revokeAllUserTokens(User user);
}
