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

// Added imports
import com.example.paymentservice.service.strategy.PaymentStrategy;
import com.example.paymentservice.service.strategy.PaymentStrategyFactory;
import com.example.paymentservice.service.strategy.PaymentProcessingResult;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final PaymentStrategyFactory paymentStrategyFactory; // Added

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, 
                          OrderServiceClient orderServiceClient, 
                          PaymentStrategyFactory paymentStrategyFactory) { // Updated constructor
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
        this.paymentStrategyFactory = paymentStrategyFactory; // Added
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        paymentRepository.findByOrderId(paymentRequest.getOrderId()).ifPresent(existingPayment -> {
            if ("SUCCESSFUL".equals(existingPayment.getStatus())) {
                throw new IllegalStateException("Payment for order " + paymentRequest.getOrderId() + " has already been processed successfully.");
            }
        });

        Payment payment = paymentRepository.findByOrderId(paymentRequest.getOrderId())
                                        .filter(p -> !"SUCCESSFUL".equals(p.getStatus()))
                                        .orElseGet(Payment::new); 

        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod()); 
        payment.setStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now()); 

        Payment savedPayment = paymentRepository.save(payment); 

        // Get strategy and process payment
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentRequest.getPaymentMethod());
        PaymentProcessingResult processingResult = strategy.executePayment(paymentRequest);

        savedPayment.setStatus(processingResult.getStatus()); 

        if (processingResult.isSuccessful()) {
            orderServiceClient.updateOrderStatus(savedPayment.getOrderId(), "PAID");
        } else {
            orderServiceClient.updateOrderStatus(savedPayment.getOrderId(), "PAYMENT_FAILED");
        }

        Payment finalPaymentState = paymentRepository.save(savedPayment); 
        return mapToPaymentResponse(finalPaymentState);
    }

    public PaymentResponse getPaymentStatusByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId)); 
        return mapToPaymentResponse(payment);
    }

    // Removed simulatePaymentGateway method

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
