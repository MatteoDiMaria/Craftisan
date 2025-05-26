package com.example.orderservice.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id; // Good to have the item's own ID
    private String productId;
    private int quantity;
    private double pricePerItem;
    private String productName;
    private double itemTotal; // Calculated as pricePerItem * quantity
}
