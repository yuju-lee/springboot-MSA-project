package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailDTO {

    private Integer productID;
    private Integer orderPrice;
    private Integer productCount;

    // 기타 필드 및 메서드
}