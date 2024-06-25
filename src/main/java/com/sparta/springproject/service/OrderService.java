package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.OrderDetailDTO;
import com.sparta.springproject.dto.OrderRequestDTO;
import com.sparta.springproject.dto.ProductOrderDTO;
import com.sparta.springproject.model.*;
import com.sparta.springproject.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final WishListRepository wishListRepository;

    public OrderService(JwtUtil jwtUtil, MemberRepository memberRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, WishListRepository wishListRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.wishListRepository = wishListRepository;
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

    @Transactional
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
        order.setOrderStatus("ORDER_CREATE");
        order.setOrderAt(LocalDateTime.now());
        OrderEntity savedOrder = orderRepository.save(order);

        for (ProductOrderDTO productOrder : orderRequestDTO.getProductInfo()) {
            Optional<ProductEntity> productOpt = productRepository.findById(Long.valueOf(productOrder.getProductNo()));
            // 제품 번호 있을 경우
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();
                int remainingStock = product.getStock() - productOrder.getQty();
                // 재고가 주문수량보다 적을 경우
                if (remainingStock < 0) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + productOrder.getProductNo());
                }
                product.setStock(remainingStock); // 재고 차감
                productRepository.save(product);

                OrderDetailEntity orderDetail = new OrderDetailEntity();
                orderDetail.setOrderKey(Long.valueOf(savedOrder.getOrderKey()));
                orderDetail.setProductID(product.getProducts_id());
                orderDetail.setOrderPrice(product.getPrice() * productOrder.getQty());
                orderDetail.setProductCount(productOrder.getQty());

                // 주문한 제품이 위시리스트에 있는 경우 삭제
                Optional<WishEntity> optionalWish = wishListRepository.findByProductIdAndEmail(product.getProducts_id(), email);
                optionalWish.ifPresent(wishListRepository::delete);

                orderDetailRepository.save(orderDetail);
            } else {
                throw new IllegalArgumentException("Product not found: " + productOrder.getProductNo());
            }
        }
        return savedOrder.getOrderKey();
    }


    @Transactional
    public void deleteOrder(String accessToken, Long orderNo) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        Optional<OrderEntity> optionalOrder = orderRepository.findById(orderNo);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        OrderEntity order = optionalOrder.get();
        if (!order.getEmail().equals(email)) {
            throw new IllegalArgumentException("You do not have access to this order");
        }

        if (!"ORDER_CREATE".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("Order cancellation is only possible before shipping.");
        }

        List<OrderDetailEntity> orderDetails = orderDetailRepository.findByOrderKey(Math.toIntExact(orderNo));
        for (OrderDetailEntity orderDetail : orderDetails) {
            Optional<ProductEntity> optionalProduct = productRepository.findById(Long.valueOf(orderDetail.getProductID()));
            if (optionalProduct.isPresent()) {
                ProductEntity product = optionalProduct.get();
                product.setStock(product.getStock() + orderDetail.getProductCount());
                productRepository.save(product);
            }
        }
        order.setOrderStatus("CANCEL_COMPLETED");
        orderRepository.save(order);
    }

    public void returnOrder(String accessToken, Long orderNo) {
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

        if (!order.getOrderStatus().equals("ORDER_COMPLETE")) {
            throw new IllegalArgumentException("Return is only allowed for completed orders.");
        }

        // 반품 완료로 상태 변경
        order.setOrderStatus("RETURN_COMPLETE");
        orderRepository.save(order);

        // 주문 상세 내역 업데이트 및 재고 증가
        List<OrderDetailEntity> orderDetails = orderDetailRepository.findByOrderKey(Math.toIntExact(orderNo));
        for (OrderDetailEntity orderDetail : orderDetails) {
            ProductEntity product = productRepository.findById(Long.valueOf(orderDetail.getProductID())).orElseThrow(
                    () -> new IllegalArgumentException("Product not found for order detail")
            );
            product.setStock(product.getStock() + orderDetail.getProductCount());
            productRepository.save(product);
        }
    }
}