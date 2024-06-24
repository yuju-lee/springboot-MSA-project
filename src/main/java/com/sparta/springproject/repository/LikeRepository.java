package com.sparta.springproject.repository;

import com.sparta.springproject.model.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
        Optional<LikeEntity> findByProductIdAndEmail(Long productId, String email);
}