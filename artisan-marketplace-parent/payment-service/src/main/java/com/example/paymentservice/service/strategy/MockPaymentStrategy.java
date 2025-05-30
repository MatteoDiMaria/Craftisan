package com.example.paymentservice.service.strategy;

import com.example.paymentservice.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired; // Added import

@Component("mockPaymentStrategy") // Qualify it for the factory
public class MockPaymentStrategy implements PaymentStrategy {

    private final Random random;

    // Constructor for testing
    public MockPaymentStrategy(Random random) {
        this.random = random;
    }

    // Default constructor for Spring
    @Autowired // Or ensure it's the one Spring calls if multiple exist and one is default
    public MockPaymentStrategy() {
        this.random = new Random();
    }

    @Override
    public PaymentProcessingResult executePayment(PaymentRequest paymentRequest) {
        String transactionId = UUID.randomUUID().toString();
        // Simulate payment processing
        // For "MOCK_CREDIT_CARD_FAIL" method, always fail.
        if ("MOCK_CREDIT_CARD_FAIL".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
            return PaymentProcessingResult.failure("Payment failed due to MOCK_CREDIT_CARD_FAIL method.", transactionId);
        }

        // Simple simulation: 80% chance of success for other mock methods
        boolean paymentSuccessful = random.nextInt(100) < 80;

        if (paymentSuccessful) {
            return PaymentProcessingResult.success(transactionId);
        } else {
            return PaymentProcessingResult.failure("Mock payment processing failed randomly.", transactionId);
        }
    }
}
