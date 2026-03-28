package com.springboot.ecommerce.dto.request;
import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
    private Integer sortOrder;
}
