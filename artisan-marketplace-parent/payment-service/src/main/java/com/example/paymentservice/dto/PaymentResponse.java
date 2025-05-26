package com.example.paymentservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private LocalDateTime paymentDate;
    private double amount;
    private String paymentMethod;
    private String status;
}
