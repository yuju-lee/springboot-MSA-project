package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.MyPageResponseDTO;
import com.sparta.springproject.dto.OrderDetailDTO;
import com.sparta.springproject.dto.OrderResponseDTO;
import com.sparta.springproject.dto.WishListRequestDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.model.OrderDetailEntity;
import com.sparta.springproject.model.OrderEntity;
import com.sparta.springproject.model.WishEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.repository.OrderDetailRepository;
import com.sparta.springproject.repository.OrderRepository;
import com.sparta.springproject.repository.WishListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyPageService {

    private final JwtUtil jwtUtil;
    private final WishListRepository wishListRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    public MyPageService(JwtUtil jwtUtil, WishListRepository wishListRepository, OrderRepository orderRepository, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.wishListRepository = wishListRepository;
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
    }

    public MyPageResponseDTO getMyPageData(String accessToken) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject(); // 토큰에서 이메일 추출

        MemberEntity memberEntity = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        List<WishListRequestDTO> wishList = wishListRepository.findByEmail(email).stream()
                .map(wish -> new WishListRequestDTO(wish.getProductName(), wish.getPrice(), wish.getStock()))
                .collect(Collectors.toList());

        List<OrderResponseDTO> orderList = orderRepository.findByEmail(email).stream()
                .map(order -> new OrderResponseDTO(order.getOrderKey(), order.getOrderStatus(), order.getOrderAt()))
                .collect(Collectors.toList());

        return new MyPageResponseDTO(wishList, orderList);
    }
}