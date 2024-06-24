package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.LikeRequestDTO;
import com.sparta.springproject.dto.ProductDTO;
import com.sparta.springproject.dto.UpdatePasswordDTO;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.model.ProductEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductService {

    private final ProductRepository productRepository;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public ProductService(ProductRepository productRepository, JwtUtil jwtUtil, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ProductDTO> getAllProducts() {
        List<ProductEntity> products = productRepository.findAll();
        return products.stream()
                .map(product -> new ProductDTO(product.getProductName(), product.getPrice()))
                .collect(Collectors.toList());
    }

    public Optional<ProductEntity> getProductById(Integer product_id) {
        return productRepository.findById(Long.valueOf(product_id));
    }

    public void likeProduct(String accessToken, LikeRequestDTO likeRequest) {

        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject(); // 토큰에서 이메일 추출
        MemberEntity memberEntity = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        Optional<ProductEntity> optionalProduct = productRepository.findById(likeRequest.getProductId());

        if (optionalProduct.isPresent()) {
            ProductEntity product = optionalProduct.get();
            product.setLikecount(product.getLikecount() + 1);
            log.info("user: {} liked {}!", email, product.getProductName());
            productRepository.save(product);
        } else {
            throw new IllegalArgumentException("Product not found");
        }
    }
}

