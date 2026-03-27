package com.springboot.ecommerce.controller;

import com.springboot.ecommerce.dto.request.CategoryRequest;
import com.springboot.ecommerce.util.ApiResponse;
import com.springboot.ecommerce.dto.response.CategoryResponse;
import com.springboot.ecommerce.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.create(request);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {

        List<CategoryResponse> categories = categoryService.getAll();

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories fetched successfully")
                        .data(categories)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {

        CategoryResponse response = categoryService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @RequestBody CategoryRequest request
    ) {

        CategoryResponse response = categoryService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        categoryService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Category deleted successfully")
                        .data(null)
                        .build()
        );
    }
}