package com.sparta.springproject.repository;

import com.sparta.springproject.model.MemberEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberRedisRepository extends CrudRepository<MemberEntity, String> {
    Optional<MemberEntity> findByEmail(String email);
}