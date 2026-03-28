package com.springboot.ecommerce.controller;

import com.springboot.ecommerce.dto.request.ProductImageRequest;
import com.springboot.ecommerce.dto.response.ProductImageResponse;
import com.springboot.ecommerce.services.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/{productId}")
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable UUID productId,
            @RequestBody ProductImageRequest request
    ) {
        return ResponseEntity.ok(productImageService.addImage(productId, request));
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getImages(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(productImageService.getImagesByProduct(productId));
    }


    @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable UUID imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
