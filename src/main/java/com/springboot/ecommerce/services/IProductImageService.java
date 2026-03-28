package com.springboot.ecommerce.services;

import com.springboot.ecommerce.dto.request.ProductImageRequest;
import com.springboot.ecommerce.dto.response.ProductImageResponse;

import java.util.List;
import java.util.UUID;

public interface IProductImageService {

    ProductImageResponse addImage(UUID productId, ProductImageRequest request);

    List<ProductImageResponse> getImagesByProduct(UUID productId);

    void deleteImage(UUID imageId);
}
