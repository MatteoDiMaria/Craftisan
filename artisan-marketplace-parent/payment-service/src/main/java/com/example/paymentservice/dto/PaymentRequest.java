package com.example.paymentservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PaymentRequest {
    private Long orderId;
    private double amount;
    private String paymentMethod; // Simplified from Map for this phase, e.g., "MOCK_CREDIT_CARD"
    // private Map<String, String> paymentMethodDetails; // For more complex scenarios
}
