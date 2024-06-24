package com.sparta.springproject.repository;

import com.sparta.springproject.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

}
