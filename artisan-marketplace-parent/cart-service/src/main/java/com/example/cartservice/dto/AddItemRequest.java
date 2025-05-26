package com.example.cartservice.dto;

import lombok.Data;

@Data
public class AddItemRequest {
    private String productId;
    private int quantity;
    private String productName; // Name of the product
    private double currentPrice; // Current price of the product, to be used as priceAtAddition
    private String productImage; // Optional product image URL
}
