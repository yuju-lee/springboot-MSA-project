package com.sparta.springproject.controller;

import com.sparta.springproject.dto.OrderDetailDTO;
import com.sparta.springproject.dto.OrderRequestDTO;
import com.sparta.springproject.model.OrderEntity;
import com.sparta.springproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order")
    public ResponseEntity<List<OrderEntity>> getOrders(@RequestHeader("Authorization") String accessToken) {
        try {
            List<OrderEntity> orders = orderService.getOrders(accessToken);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/order/{orderNo}")
    public ResponseEntity<List<OrderDetailDTO>> getOrderDetails(@RequestHeader("Authorization") String accessToken, @PathVariable Long orderNo) {
        List<OrderDetailDTO> orderDetails = orderService.getOrderDetails(accessToken, orderNo);
        return ResponseEntity.ok(orderDetails);
    }

    @PostMapping("/order")
    public ResponseEntity<String> createOrder(@RequestHeader("Authorization") String accessToken, @RequestBody OrderRequestDTO orderRequestDTO) {
        Long orderNumber = Long.valueOf(orderService.createOrder(accessToken, orderRequestDTO));
        String message = String.format("Order is completed. Order number is: %d", orderNumber);
        return ResponseEntity.ok(message);
    }

}