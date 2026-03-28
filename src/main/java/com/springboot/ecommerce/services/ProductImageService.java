package com.springboot.ecommerce.services;

import com.springboot.ecommerce.dto.request.ProductImageRequest;
import com.springboot.ecommerce.dto.response.ProductImageResponse;
import com.springboot.ecommerce.entity.Product;
import com.springboot.ecommerce.entity.ProductImage;
import com.springboot.ecommerce.repository.ProductImageRepository;
import com.springboot.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductImageResponse addImage(UUID productId, ProductImageRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl())
                .altText(request.getAltText())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        ProductImage saved = productImageRepository.save(image);

        return mapToResponse(saved);
    }

    @Override
    public List<ProductImageResponse> getImagesByProduct(UUID productId) {
        return productImageRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteImage(UUID imageId) {
        productImageRepository.deleteById(imageId);
    }

    private ProductImageResponse mapToResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId().toString())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
