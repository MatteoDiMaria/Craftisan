package com.example.paymentservice.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentStrategyFactoryTest {

    private PaymentStrategyFactory factory;
    private PaymentStrategy mockPaymentStrategyBean; // Represents the bean named "mockPaymentStrategy"

    @BeforeEach
    void setUp() {
        // This simulates how Spring would inject the map of beans.
        // The key in the map is the bean name.
        mockPaymentStrategyBean = Mockito.mock(PaymentStrategy.class); // Using a generic mock for the bean
        Map<String, PaymentStrategy> strategyMap = new HashMap<>();
        strategyMap.put("mockPaymentStrategy", mockPaymentStrategyBean);
        
        factory = new PaymentStrategyFactory(strategyMap);
    }

    @Test
    void getStrategy_returnsMockStrategyBean_forKnownMockMethods() {
        assertSame(mockPaymentStrategyBean, factory.getStrategy("MOCK_CREDIT_CARD"));
        assertSame(mockPaymentStrategyBean, factory.getStrategy("MOCK_CREDIT_CARD_FAIL"));
    }

    @Test
    void getStrategy_throwsException_forUnknownMethod() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.getStrategy("UNKNOWN_PAYMENT_METHOD");
        });
        assertEquals("Unknown payment method: UNKNOWN_PAYMENT_METHOD", exception.getMessage());
    }
}
