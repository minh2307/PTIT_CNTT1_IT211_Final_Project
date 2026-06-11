package com.example.finalproject.service;

public interface RedisBlacklistService {
    void blacklistToken(String token, long ttlInMs);
    boolean isBlacklisted(String token);
    void removeToken(String token);
}
