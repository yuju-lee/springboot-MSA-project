package com.sparta.springproject.service;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.ProductDTO;
import com.sparta.springproject.dto.WishListRequestDTO;
import com.sparta.springproject.model.LikeEntity;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.model.ProductEntity;
import com.sparta.springproject.model.WishEntity;
import com.sparta.springproject.repository.LikeRepository;
import com.sparta.springproject.repository.JpaMemberRepository;
import com.sparta.springproject.repository.ProductRepository;
import com.sparta.springproject.repository.WishListRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductService {

    private final ProductRepository productRepository;
    private final JwtUtil jwtUtil;
    private final JpaMemberRepository jpaMemberRepository;
    private final LikeRepository likeRepository;
    private final WishListRepository wishListRepository;


    public ProductService(ProductRepository productRepository, JwtUtil jwtUtil, JpaMemberRepository jpaMemberRepository, LikeRepository likeRepository, WishListRepository wishListRepository) {
        this.productRepository = productRepository;
        this.jwtUtil = jwtUtil;
        this.jpaMemberRepository = jpaMemberRepository;
        this.likeRepository = likeRepository;
        this.wishListRepository = wishListRepository;
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

    @Transactional
    public String toggleLikeProduct(String accessToken, Long productId) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity memberEntity = jpaMemberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found!")
        );

        Optional<ProductEntity> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        ProductEntity product = optionalProduct.get();
        Optional<LikeEntity> optionalLike = likeRepository.findByProductIdAndEmail(productId, email);

        if (optionalLike.isPresent()) {
            likeRepository.delete(optionalLike.get());
            product.setLikecount(product.getLikecount() - 1);
            productRepository.save(product);
            return "Unlike...";
        } else {
            LikeEntity like = new LikeEntity();
            like.setProductId(productId);
            like.setEmail(email);
            likeRepository.save(like);

            product.setLikecount(product.getLikecount() + 1);
            productRepository.save(product);
            return "Like!";
        }
    }

    public List<WishListRequestDTO> getWishProducts(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or missing token");
        }

        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        List<WishEntity> wishs = wishListRepository.findByEmail(email);
        return wishs.stream()
                .map(wish -> new WishListRequestDTO(wish.getProductId(), wish.getProductName(), wish.getPrice(), wish.getStock()))
                .collect(Collectors.toList());
    }

    public void addWishProduct(HttpServletRequest request, WishListRequestDTO wishListRequestDTO) {
        String token = request.getHeader("Authorization").replace(JwtUtil.BEARER_PREFIX, "");
        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity member = jpaMemberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<ProductEntity> optionalProduct = productRepository.findById(Long.valueOf(wishListRequestDTO.getProductId()));
        if (optionalProduct.isPresent()) {
            ProductEntity product = optionalProduct.get();

            WishEntity wish = new WishEntity();
            wish.setProductId(product.getProducts_id());
            wish.setProductName(product.getProductName());
            wish.setPrice(product.getPrice() * wishListRequestDTO.getStock());
            wish.setStock(wishListRequestDTO.getStock());
            wish.setEmail(email);

            wishListRepository.save(wish);
        } else {
            throw new IllegalArgumentException("Product not found");
        }
    }

    public void deleteWishProduct(String accessToken, Long productId) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");

        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity member = jpaMemberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        Optional<WishEntity> optionalWish = wishListRepository.findByProductIdAndEmail(Math.toIntExact(productId), email);
        if (optionalWish.isPresent()) {
            WishEntity wishEntity = optionalWish.get();
            wishListRepository.delete(wishEntity);
        } else {
            throw new IllegalArgumentException("Wish product not found for the user");
        }
    }

}