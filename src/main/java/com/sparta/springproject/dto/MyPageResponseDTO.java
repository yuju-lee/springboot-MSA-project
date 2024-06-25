package com.sparta.springproject.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class MyPageResponseDTO {
    private final List<WishListRequestDTO> wishList;
    private final List<OrderResponseDTO> orderList;

    public MyPageResponseDTO(List<WishListRequestDTO> wishList, List<OrderResponseDTO> orderList) {
        this.wishList = wishList;
        this.orderList = orderList;
    }

}
