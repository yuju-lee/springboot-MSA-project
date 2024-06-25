package com.sparta.springproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orderdetail")
@NoArgsConstructor
@Getter
@Setter
public class OrderDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderDetailKey;

    private Long orderKey;
    private Integer productID;
    private Integer orderPrice;
    private Integer productCount;
}