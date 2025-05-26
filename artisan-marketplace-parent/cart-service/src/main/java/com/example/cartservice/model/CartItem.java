package com.example.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private int quantity;
    private double priceAtAddition; // Price of the product when it was added to cart
    private String productName;
    private String productImage; // Optional URL to an image
}
