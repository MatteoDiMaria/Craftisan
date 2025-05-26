package com.example.orderservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String userId;
    private String shippingAddress;
    private List<OrderItemRequest> items;
}
