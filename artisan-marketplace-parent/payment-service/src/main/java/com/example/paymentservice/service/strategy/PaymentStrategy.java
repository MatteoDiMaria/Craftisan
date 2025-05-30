package com.example.paymentservice.service.strategy;

import com.example.paymentservice.dto.PaymentRequest;

public interface PaymentStrategy {
    PaymentProcessingResult executePayment(PaymentRequest paymentRequest);
}
