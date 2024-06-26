package com.sparta.springproject.repository;

import com.sparta.springproject.model.WishEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface WishListRepository extends JpaRepository<WishEntity, Long> {
    List<WishEntity> findByEmail(String email);
    Optional<WishEntity> findByProductIdAndEmail(Integer productId, String email);
}