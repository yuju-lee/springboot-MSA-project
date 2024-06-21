package com.sparta.springproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, Boolean> redisTemplate;

    @Autowired
    public TokenBlacklistService(RedisTemplate<String, Boolean> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, true);
        redisTemplate.expire(token, 1, TimeUnit.HOURS); // 블랙리스트에 1시간 동안 유지
    }

    public boolean isBlacklisted(String token) {
        Boolean isBlacklisted = redisTemplate.opsForValue().get(token);
        return isBlacklisted != null && isBlacklisted;
    }
}