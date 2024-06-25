package com.sparta.springproject.repository;

import com.sparta.springproject.model.WishEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface WishListRepository extends JpaRepository<WishEntity, Long> {
    List<WishEntity> findByEmail(String email);
    Optional<WishEntity> findByProductIdAndEmail(Integer productId, String email);
}