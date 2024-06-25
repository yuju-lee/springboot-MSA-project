package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WishListRequestDTO {
    private Integer productId;
    private String productName;
    private Integer price;
    private Integer stock;

    // 위시리스트에 넣을 때 쓰는 거
    public WishListRequestDTO(String productName, Integer stock) {
        this.productName = productName;
        this.stock = stock;
    }

    // 조회할 때 쓰는 거
    public WishListRequestDTO(String productName, Integer price, Integer stock) {
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    public WishListRequestDTO(Integer productId, String productName, Integer price, Integer stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }
}