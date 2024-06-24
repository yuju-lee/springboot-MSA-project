package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishListRequestDTO {
    private Long productId;
    private String productName;
    private Integer price;
    private Integer stock;

    // 기본 생성자
    public WishListRequestDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public WishListRequestDTO(Long productId, String productName, Integer price, Integer stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    // 일부 필드를 포함하는 생성자
    public WishListRequestDTO(String productName, Integer price) {
        this.productName = productName;
        this.price = price;
    }
}
