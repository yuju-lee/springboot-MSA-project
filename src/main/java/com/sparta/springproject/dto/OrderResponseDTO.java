package com.sparta.springproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderKey;
    private String orderStatus;
    private LocalDateTime orderAt;

    public OrderResponseDTO(Integer orderKey, String orderStatus, LocalDateTime orderAt) {
        this.orderKey = Long.valueOf(orderKey);
        this.orderStatus = orderStatus;
        this.orderAt = orderAt;
    }
}