package com.example.cartservice.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    private String productId;
    private int quantity;
    private double priceAtAddition;
    private String productName;
    private String productImage;
    private double itemTotal; // Calculated as priceAtAddition * quantity
}
