package com.example.productservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductUpdateRequest {
    // All fields are optional for partial updates
    private String name;
    private String description;
    private Double price; // Use Double object type to allow null
    private String category;
    private List<String> images;
    private Integer stockQuantity; // Use Integer object type to allow null
    private Map<String, String> details;
    // artisanId is typically not updatable for a product
}
