package com.springboot.ecommerce.services;

import com.springboot.ecommerce.dto.request.ProductRequest;
import com.springboot.ecommerce.dto.response.ProductImageResponse;
import com.springboot.ecommerce.dto.response.ProductResponse;
import com.springboot.ecommerce.entity.Category;
import com.springboot.ecommerce.entity.Product;
import com.springboot.ecommerce.entity.ProductImage;
import com.springboot.ecommerce.repository.CategoryRepository;
import com.springboot.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .weightKg(request.getWeightKg())
                .category(category)
                .build();

        if (request.getImageUrl() != null) {

            ProductImage image = ProductImage.builder()
                    .imageUrl(request.getImageUrl())
                    .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
                    .product(product)
                    .build();

            product.getImages().add(image);
        }

        product = productRepository.save(product);

        return mapToResponse(product);
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public ProductResponse getById(UUID id) {
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public ProductResponse update(UUID id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setWeightKg(request.getWeightKg());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        return mapToResponse(productRepository.save(product));
    }

    public void delete(UUID id) {
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {

        List<ProductImageResponse> images = product.getImages() == null
                ? List.of()
                : product.getImages().stream()
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId().toString())
                        .imageUrl(img.getImageUrl())
                        .altText(img.getAltText())
                        .isPrimary(img.getIsPrimary())
                        .sortOrder(img.getSortOrder())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .categoryName(product.getCategory().getName())
                .categoryId(product.getCategory().getId())
                .stockQuantity(product.getStockQuantity())
                .images(images)
                .build();
    }
}