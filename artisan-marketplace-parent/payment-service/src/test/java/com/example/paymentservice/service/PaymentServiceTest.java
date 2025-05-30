package com.example.paymentservice.service;

import com.example.paymentservice.client.OrderServiceClient;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
// Added imports for strategy
import com.example.paymentservice.service.strategy.PaymentStrategy;
import com.example.paymentservice.service.strategy.PaymentStrategyFactory;
import com.example.paymentservice.service.strategy.PaymentProcessingResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Added import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List; // Added import
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; // Added import
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private PaymentStrategyFactory paymentStrategyFactory; // Added mock

    @Mock
    private PaymentStrategy mockPaymentStrategy; // Added mock

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment; // Represents an existing payment record for some tests

    @BeforeEach
    void setUp() {
        // Common setup for payment request
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(1L);
        paymentRequest.setAmount(100.0);
        paymentRequest.setPaymentMethod("MOCK_CREDIT_CARD");

        // Common setup for payment entity
        payment = new Payment();
        payment.setId(1L); // Simulate existing payment ID
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        // payment.setStatus() will be set per test case

        // Mock the factory to return our generic mock strategy for any payment method string
        when(paymentStrategyFactory.getStrategy(anyString())).thenReturn(mockPaymentStrategy);
    }

    @Test
    void processPayment_successful() {
        paymentRequest.setOrderId(1L); // Ensure orderId is set for this test
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(mockPaymentStrategy.executePayment(any(PaymentRequest.class)))
                .thenReturn(PaymentProcessingResult.success("mock-tx-id"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(System.nanoTime()); // Simulate ID generation if new
            return p;
        });

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals("SUCCESSFUL", response.getStatus());
        assertEquals(paymentRequest.getOrderId(), response.getOrderId());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        List<Payment> capturedPayments = paymentCaptor.getAllValues();
        assertEquals("PENDING", capturedPayments.get(0).getStatus());
        assertEquals("SUCCESSFUL", capturedPayments.get(1).getStatus());

        verify(orderServiceClient).updateOrderStatus(1L, "PAID");
    }

    @Test
    void processPayment_failed() {
        paymentRequest.setOrderId(1L);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(mockPaymentStrategy.executePayment(any(PaymentRequest.class)))
                .thenReturn(PaymentProcessingResult.failure("mock-error", "mock-tx-id-fail"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(System.nanoTime()); // Simulate ID generation
            return p;
        });

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals(paymentRequest.getOrderId(), response.getOrderId());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        List<Payment> capturedPayments = paymentCaptor.getAllValues();
        assertEquals("PENDING", capturedPayments.get(0).getStatus());
        assertEquals("FAILED", capturedPayments.get(1).getStatus());

        verify(orderServiceClient).updateOrderStatus(1L, "PAYMENT_FAILED");
    }
    
    @Test
    void processPayment_updatesExistingFailedPayment_successfully() {
        Long orderIdForRetry = 2L;
        paymentRequest.setOrderId(orderIdForRetry); // Target a different order

        Payment existingFailedPayment = new Payment();
        existingFailedPayment.setId(20L); // Existing ID
        existingFailedPayment.setOrderId(orderIdForRetry);
        existingFailedPayment.setAmount(150.0);
        existingFailedPayment.setPaymentMethod("MOCK_CREDIT_CARD");
        existingFailedPayment.setStatus("FAILED"); // It previously failed
        existingFailedPayment.setPaymentDate(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findByOrderId(orderIdForRetry)).thenReturn(Optional.of(existingFailedPayment));
        when(mockPaymentStrategy.executePayment(any(PaymentRequest.class)))
                .thenReturn(PaymentProcessingResult.success("mock-tx-id-retry"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0)); // Echo back

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertEquals("SUCCESSFUL", response.getStatus());
        assertEquals(orderIdForRetry, response.getOrderId());
        assertEquals(existingFailedPayment.getId(), response.getPaymentId()); // Should reuse the ID

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        // The first save is for PENDING on the existing entity, second for SUCCESSFUL
        verify(paymentRepository, times(2)).save(paymentCaptor.capture()); 
        Payment savedPayment = paymentCaptor.getAllValues().get(1); // Get the final state

        assertEquals("SUCCESSFUL", savedPayment.getStatus());
        assertEquals(existingFailedPayment.getId(), savedPayment.getId()); // Verify ID is maintained
        assertEquals(paymentRequest.getAmount(), savedPayment.getAmount()); // Amount might be updated from request
        assertNotEquals(existingFailedPayment.getPaymentDate(), savedPayment.getPaymentDate()); // Date should be updated

        verify(orderServiceClient).updateOrderStatus(orderIdForRetry, "PAID");
    }


    @Test
    void processPayment_alreadyProcessedSuccessfully_throwsException() {
        // Use the common 'payment' object which has ID 1L and orderId 1L
        payment.setStatus("SUCCESSFUL"); 
        when(paymentRepository.findByOrderId(paymentRequest.getOrderId())).thenReturn(Optional.of(payment));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(paymentRequest); // paymentRequest targets orderId 1L
        });
        assertEquals("Payment for order " + paymentRequest.getOrderId() + " has already been processed successfully.", exception.getMessage());
        // Verify no new save or status update if already successful
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderServiceClient, never()).updateOrderStatus(anyLong(), anyString());
    }


    @Test
    void getPaymentStatusByOrderId_found() {
        // Use the common 'payment' object which has ID 1L and orderId 1L
        payment.setStatus("SUCCESSFUL");
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentStatusByOrderId(1L);

        assertNotNull(response);
        assertEquals(payment.getId(), response.getPaymentId());
        assertEquals(payment.getStatus(), response.getStatus());
        assertEquals(payment.getOrderId(), response.getOrderId());
    }

    @Test
    void getPaymentStatusByOrderId_notFound() {
        Long nonExistentOrderId = 99L;
        when(paymentRepository.findByOrderId(nonExistentOrderId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentStatusByOrderId(nonExistentOrderId);
        });
        assertEquals("Payment not found for orderId: " + nonExistentOrderId, exception.getMessage());
    }
}
