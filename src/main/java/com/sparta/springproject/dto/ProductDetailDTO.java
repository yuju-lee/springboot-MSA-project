package com.sparta.springproject.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductDetailDTO {

        private String productName;
        private Integer price;
        private Integer stock;
        private Integer likecount;
}
