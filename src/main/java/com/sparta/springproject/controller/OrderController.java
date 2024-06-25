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
        try {
            Integer orderKey = orderService.createOrder(accessToken, orderRequestDTO);
            return ResponseEntity.ok("Order created successfully. Order number is: " + orderKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/order/{orderNo}")
    public ResponseEntity<String> cancelOrder(@RequestHeader("Authorization") String accessToken, @PathVariable Long orderNo) {
        try {
            orderService.deleteOrder(accessToken, orderNo);
            return ResponseEntity.ok("Order cancellation completed successfully. Order number is: " + orderNo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/order/return/{orderNo}")
    public ResponseEntity<String> returnOrder(@RequestHeader("Authorization") String accessToken, @PathVariable Long orderNo) {
        try {
            orderService.returnOrder(accessToken, orderNo);
            return ResponseEntity.ok("Order returned successfully. Order number is: " + orderNo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}