package com.springboot.ecommerce.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryRequest {
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private UUID parentId; // for nested category
}