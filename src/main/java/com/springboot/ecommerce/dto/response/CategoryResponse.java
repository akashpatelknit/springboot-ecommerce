package com.springboot.ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private UUID parentId;
}