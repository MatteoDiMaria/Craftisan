package com.example.orderservice.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String productId;
    private int quantity;
    private double pricePerItem;
    private String productName;
}
