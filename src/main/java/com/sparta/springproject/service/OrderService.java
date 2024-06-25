package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.OrderDetailDTO;
import com.sparta.springproject.dto.OrderRequestDTO;
import com.sparta.springproject.dto.ProductOrderDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.model.OrderDetailEntity;
import com.sparta.springproject.model.OrderEntity;
import com.sparta.springproject.model.ProductEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.repository.OrderDetailRepository;
import com.sparta.springproject.repository.OrderRepository;
import com.sparta.springproject.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    public OrderService(JwtUtil jwtUtil, MemberRepository memberRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
    }

    public List<OrderEntity> getOrders(String accessToken) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        return orderRepository.findByEmail(email);
    }

    public List<OrderDetailDTO> getOrderDetails(String accessToken, Long orderNo) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtUtil.getUserInfoFromToken(token).getSubject();
        OrderEntity order = orderRepository.findById(orderNo).orElseThrow(
                () -> new IllegalArgumentException("Order not found")
        );

        if (!order.getEmail().equals(email)) {
            throw new IllegalArgumentException("You do not have access to this order");
        }

        List<OrderDetailEntity> orderDetails = orderDetailRepository.findByOrderKey(Math.toIntExact(orderNo));
        return orderDetails.stream()
                .map(detail -> {
                    OrderDetailDTO dto = new OrderDetailDTO();
                    dto.setProductID(detail.getProductID());
                    dto.setOrderPrice(detail.getOrderPrice());
                    dto.setProductCount(detail.getProductCount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Integer createOrder(String accessToken, OrderRequestDTO orderRequestDTO) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        OrderEntity order = new OrderEntity();
        order.setEmail(email);
        order.setOrderStatus("ORDER_CREATE"); // 주문 상태 셋업
        order.setOrderAt(LocalDateTime.now()); // 주문 날짜-시간
        OrderEntity savedOrder = orderRepository.save(order);

        // 주문 상세 정보 저장하는데, for로 상세 주문이 들어 있는 만큼 받기 1주문서에는 n개의 주문이 있으니깐
        for (ProductOrderDTO productOrder : orderRequestDTO.getProductInfo()) {
            Optional<ProductEntity> productOpt = productRepository.findById(Long.valueOf(productOrder.getProductNo())); // 타입도 통일해야지 이거 원
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();

                OrderDetailEntity orderDetail = new OrderDetailEntity();
                orderDetail.setOrderKey(Long.valueOf(savedOrder.getOrderKey()));
                orderDetail.setProductID(product.getProducts_id());
                orderDetail.setOrderPrice(product.getPrice() * productOrder.getQty());
                orderDetail.setProductCount(productOrder.getQty());

                orderDetailRepository.save(orderDetail);
            } else {
                throw new IllegalArgumentException("Product not found: " + productOrder.getProductNo());
            }
        }
        // 생성된 주문 번호 반환
        return savedOrder.getOrderKey();
    }
}