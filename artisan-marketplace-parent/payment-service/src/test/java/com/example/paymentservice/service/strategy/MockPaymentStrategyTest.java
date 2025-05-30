package com.example.paymentservice.service.strategy;

import com.example.paymentservice.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class MockPaymentStrategyTest {

    @Test
    void executePayment_alwaysFails_forFailCardMethod() {
        // Use the default constructor for this test as Spring would
        MockPaymentStrategy strategy = new MockPaymentStrategy(); 
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOCK_CREDIT_CARD_FAIL");
        request.setAmount(100.0);
        request.setOrderId(1L);

        PaymentProcessingResult result = strategy.executePayment(request);

        assertFalse(result.isSuccessful());
        assertEquals("FAILED", result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertEquals("Payment failed due to MOCK_CREDIT_CARD_FAIL method.", result.getErrorMessage());
        assertNotNull(result.getTransactionId());
    }

    @Test
    void executePayment_canSucceed_forStandardMockCard_withPredictableRandom() {
        Random predictableRandom = new Random(1L); // Seed 1: nextInt(100) is 48 -> success
        MockPaymentStrategy strategy = new MockPaymentStrategy(predictableRandom);
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOCK_CREDIT_CARD");
        request.setAmount(100.0);
        request.setOrderId(1L);

        PaymentProcessingResult result = strategy.executePayment(request);

        assertTrue(result.isSuccessful());
        assertEquals("SUCCESSFUL", result.getStatus());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getTransactionId());
    }
    
    @Test
    void executePayment_canFail_forStandardMockCard_withPredictableRandom() {
        Random predictableRandomToFail = new Random(6L); // Seed 6: nextInt(100) is 80 -> failure
        MockPaymentStrategy strategy = new MockPaymentStrategy(predictableRandomToFail);
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOCK_CREDIT_CARD");
        request.setAmount(100.0);
        request.setOrderId(2L);

        PaymentProcessingResult result = strategy.executePayment(request);

        assertFalse(result.isSuccessful());
        assertEquals("FAILED", result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertEquals("Mock payment processing failed randomly.", result.getErrorMessage());
        assertNotNull(result.getTransactionId());
    }
}
