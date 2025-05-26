package com.example.productsearchservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

// This class is a representation of the Product document in MongoDB.
// It's used by MongoTemplate to map query results.
// It should mirror the structure of the products stored by product-service.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products") // Specify the collection name
public class Product {

    @Id
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
