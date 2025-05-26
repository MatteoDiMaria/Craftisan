package com.example.productsearchservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponse {
    private String id;
    private String artisanId;
    private String name;
    private String description;
    private double price;
    private String category;
    private List<String> images;
    private int stockQuantity; // May or may not be relevant for search results, but good to have
    private Map<String, String> details;
}
