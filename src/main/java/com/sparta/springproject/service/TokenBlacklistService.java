package com.sparta.springproject.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, true);
        redisTemplate.expire(token, 1, TimeUnit.HOURS); // 토큰의 유효 기간 동안만 블랙리스트에 존재
    }

    public boolean isBlacklisted(String token) {
        return !Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    public void storeRefreshToken(String name, String refreshToken) {
        String key = "refreshToken:" + name;
        redisTemplate.opsForValue().set(key, refreshToken);
        redisTemplate.expire(key, 1, TimeUnit.DAYS); // Refresh Token의 유효 기간 설정
    }
    }
}
