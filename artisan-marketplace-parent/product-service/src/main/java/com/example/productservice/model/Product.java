package com.example.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id; // MongoDB typically uses String IDs

    private String artisanId; // Assuming User ID is String, adjust if it's Long
    private String name;
    private String description;
    private double price;
    private String category;
    private List<String> images; // URLs to images
    private int stockQuantity;
    private Map<String, String> details; // e.g., material, dimensions, weight
}
