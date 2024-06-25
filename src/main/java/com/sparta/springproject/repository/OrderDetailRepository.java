package com.sparta.springproject.repository;

import com.sparta.springproject.model.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Integer> {
    List<OrderDetailEntity> findByOrderKey(Integer orderKey);
}