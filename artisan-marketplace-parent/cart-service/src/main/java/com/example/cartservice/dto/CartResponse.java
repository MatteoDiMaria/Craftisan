package com.example.cartservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartResponse {
    private String cartId; // This will be the userId
    private List<CartItemResponse> items;
    private double provisionalTotal;
}
