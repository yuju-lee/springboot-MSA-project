package com.sparta.springproject.repository;

import com.sparta.springproject.model.MemberEntity;
import org.springframework.data.repository.CrudRepository;

public interface MemberRedisRepository extends CrudRepository<MemberEntity, Long> {
    // Redis 관련 메서드
}