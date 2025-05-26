package com.example.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId; // Renamed from id for clarity
    private String userId;
    private LocalDateTime orderDate;
    private String status;
    private double totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items;
}
