package com.sparta.springproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore // JSON 응답에서 productId 필드를 무시
    private Integer products_id;

    private String productName;
    private Integer price;
    private Integer stock;
    private Integer likecount;

}
