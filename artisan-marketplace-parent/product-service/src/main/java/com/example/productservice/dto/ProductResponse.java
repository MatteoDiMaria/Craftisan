package com.example.productservice.dto;

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
    private int stockQuantity;
    private Map<String, String> details;
}
