package com.example.paymentservice.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategyMap;

    @Autowired
    public PaymentStrategyFactory(Map<String, PaymentStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public PaymentStrategy getStrategy(String paymentMethod) {
        // Normalize the paymentMethod to a bean name convention if needed
        // e.g., "MOCK_CREDIT_CARD" -> "mockCreditCardStrategy"
        // For now, let's assume paymentMethod directly maps to a simplified bean name or we look it up.
        // A more robust solution might involve a mapping configuration.

        PaymentStrategy strategy = null;
        if ("MOCK_CREDIT_CARD".equalsIgnoreCase(paymentMethod) || 
            "MOCK_CREDIT_CARD_FAIL".equalsIgnoreCase(paymentMethod)) {
            strategy = strategyMap.get("mockPaymentStrategy");
        }
        // Add more else-if blocks for other payment methods and their corresponding strategy beans.
        // e.g., else if ("STRIPE_CARD".equalsIgnoreCase(paymentMethod)) {
        // strategy = strategyMap.get("stripePaymentStrategy");
        // }


        return Optional.ofNullable(strategy)
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + paymentMethod));
    }
}
