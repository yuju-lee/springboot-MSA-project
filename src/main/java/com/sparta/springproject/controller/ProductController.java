package com.sparta.springproject.controller;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.dto.*;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.model.ProductEntity;
import com.sparta.springproject.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    // 상품리스트 조회
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        Object response = productService.getAllProducts();
        if (response instanceof ErrorResponse) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{products_id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer products_id) {
        ProductEntity product = productService.getProductById(Math.toIntExact(Long.valueOf(products_id)))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        productDetailDTO.setProductName(product.getProductName());
        productDetailDTO.setPrice(product.getPrice());
        productDetailDTO.setStock(product.getStock());
        productDetailDTO.setLikecount(product.getLikecount());

        return ResponseEntity.ok(productDetailDTO);
    }

    @PostMapping("/like")
    public ResponseEntity<String> likeProduct(@RequestHeader("Authorization") String token, @RequestBody LikeRequestDTO likeRequest) {
        String message = productService.toggleLikeProduct(token, likeRequest.getProductId());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/wishlist")
    public List<WishListRequestDTO> getWishList(HttpServletRequest request) {
        return productService.getAllwishProducts(request);
    }

    @PostMapping("/wishlist")
    public ResponseEntity<String> addWishProduct(@RequestBody WishListRequestDTO wishListRequestDTO, HttpServletRequest request) {
        productService.addWishProduct(request, wishListRequestDTO);
        return ResponseEntity.ok("Product added to wishlist successfully");
    }
}