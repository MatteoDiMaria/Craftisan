package com.example.cartservice.model;

import lombok.Data;
import lombok.NoArgsConstructor; // For Spring Data MongoDB
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor // Default constructor is needed for MongoDB mapping
@Document(collection = "carts")
public class Cart {

    @Id
    private String id; // This will be the userId

    private List<CartItem> items = new ArrayList<>();

    private Date lastModified;

    // Constructor that takes userId as id
    public Cart(String id) {
        this.id = id;
        this.lastModified = new Date();
    }
}
