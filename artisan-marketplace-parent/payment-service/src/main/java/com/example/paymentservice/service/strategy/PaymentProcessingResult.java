package com.example.paymentservice.service.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessingResult {
    private boolean successful;
    private String transactionId; // Can be null if not applicable or if failed
    private String errorMessage; // Can be null if successful
    private String status; // e.g., "SUCCESSFUL", "FAILED"

    // Helper factory methods
    public static PaymentProcessingResult success(String transactionId) {
        return new PaymentProcessingResult(true, transactionId, null, "SUCCESSFUL");
    }

    public static PaymentProcessingResult failure(String errorMessage) {
        return new PaymentProcessingResult(false, null, errorMessage, "FAILED");
    }
     public static PaymentProcessingResult failure(String errorMessage, String transactionId) {
        return new PaymentProcessingResult(false, transactionId, errorMessage, "FAILED");
    }
}
