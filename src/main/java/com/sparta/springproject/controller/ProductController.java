package com.sparta.springproject.controller;

import com.sparta.springproject.dto.*;
import com.sparta.springproject.model.ProductEntity;
import com.sparta.springproject.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{productsId}")
    public ResponseEntity<?> getProductById(@PathVariable Integer productsId) {
        ProductEntity product = productService.getProductById(Math.toIntExact(Long.valueOf(productsId)))
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

    @GetMapping("/wish-list")
    public ResponseEntity<List<WishListRequestDTO>> getAllWishProducts(HttpServletRequest request) {
        List<WishListRequestDTO> wishList = productService.getWishProducts(request);
        return ResponseEntity.ok(wishList);
    }


    @PostMapping("/wish-list")
    public ResponseEntity<String> addWishProduct(@RequestBody WishListRequestDTO wishListRequestDTO, HttpServletRequest request) {
        productService.addWishProduct(request, wishListRequestDTO);
        return ResponseEntity.ok("Product added to wishlist successfully");
    }

    @DeleteMapping("/wish-list/{productId}")
    public ResponseEntity<String> deleteWishProduct(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
        productService.deleteWishProduct(productId, token);
        return ResponseEntity.ok("Wishlist item deleted successfully");
    }
}