package com.example.paymentservice.service;

import com.example.paymentservice.client.OrderServiceClient;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final Random random = new Random(); // For simulating payment success/failure

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, OrderServiceClient orderServiceClient) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        // Check if payment for this order already exists and is successful
        paymentRepository.findByOrderId(paymentRequest.getOrderId()).ifPresent(existingPayment -> {
            if ("SUCCESSFUL".equals(existingPayment.getStatus())) {
                throw new IllegalStateException("Payment for order " + paymentRequest.getOrderId() + " has already been processed successfully.");
            }
            // If payment exists but failed or is pending, could allow retry or handle as per business logic.
            // For simplicity, we'll create a new payment attempt record or update if suitable.
            // Here, we assume new attempt or overwrite if exists and not successful.
        });


        Payment payment = new Payment();
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod()); // Was: paymentRequest.getPaymentMethodDetails().getOrDefault("method", "UNKNOWN")
        payment.setStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now()); // Or use @PrePersist

        Payment savedPayment = paymentRepository.save(payment);

        // Simulate payment processing
        boolean paymentSuccessful = simulatePaymentGateway(paymentRequest);

        if (paymentSuccessful) {
            savedPayment.setStatus("SUCCESSFUL");
            orderServiceClient.updateOrderStatus(savedPayment.getOrderId(), "PAID");
        } else {
            savedPayment.setStatus("FAILED");
            orderServiceClient.updateOrderStatus(savedPayment.getOrderId(), "PAYMENT_FAILED");
        }

        Payment finalPaymentState = paymentRepository.save(savedPayment);
        return mapToPaymentResponse(finalPaymentState);
    }

    public PaymentResponse getPaymentStatusByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId)); // Consider custom exception
        return mapToPaymentResponse(payment);
    }

    private boolean simulatePaymentGateway(PaymentRequest paymentRequest) {
        // Simple simulation: 50/50 chance of success
        // In a real scenario, this would involve calling an actual payment gateway API
        // and handling its response.
        // For "MOCK_CREDIT_CARD_FAIL" method, always fail.
        if ("MOCK_CREDIT_CARD_FAIL".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
            return false;
        }
        return random.nextBoolean();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        return response;
    }
}
