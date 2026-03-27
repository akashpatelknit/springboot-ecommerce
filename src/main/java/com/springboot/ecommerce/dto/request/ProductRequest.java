package com.springboot.ecommerce.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private BigDecimal weightKg;
    private UUID categoryId;
}