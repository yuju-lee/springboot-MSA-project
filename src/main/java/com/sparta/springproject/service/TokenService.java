package com.sparta.springproject.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    public TokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set(username, refreshToken, 14, TimeUnit.DAYS); // Refresh Token 저장
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username); // Refresh Token 삭제
    }

    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, "blacklisted", 60, TimeUnit.MINUTES); // 블랙리스트에 토큰 추가
    }

}
