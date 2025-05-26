package com.example.paymentservice.controller;

import com.example.paymentservice.PaymentServiceApplication;
import com.example.paymentservice.client.OrderServiceClient;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@SpringBootTest(classes = PaymentServiceApplication.class)
@AutoConfigureMockMvc
public class PaymentResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private OrderServiceClient orderServiceClient; // Mock the client

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(100L);
        paymentRequest.setAmount(250.75);
        paymentRequest.setPaymentMethod("MOCK_VISA_SUCCESS"); // Simulate a method that should succeed
    }

    @Test
    void processPayment_success() throws Exception {
        // Mock repository: no existing payment, save returns the payment
        when(paymentRepository.findByOrderId(paymentRequest.getOrderId())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L); // Simulate ID generation
            // The service saves twice: once PENDING, once with final status
            // We need to ensure the mocked save operation is flexible enough or test service layer for this detail
            return p; 
        });
        // Mock client call (void method)
        doNothing().when(orderServiceClient).updateOrderStatus(anyLong(), anyString());

        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", notNullValue()))
                .andExpect(jsonPath("$.orderId", is(paymentRequest.getOrderId().intValue())))
                .andExpect(jsonPath("$.status", is("SUCCESSFUL"))) // Assuming MOCK_VISA_SUCCESS leads to success
                .andExpect(jsonPath("$.amount", is(paymentRequest.getAmount())));

        verify(orderServiceClient).updateOrderStatus(paymentRequest.getOrderId(), "PAID");
        verify(paymentRepository, times(2)).save(any(Payment.class)); // Initial PENDING, then SUCCESSFUL
    }
    
    @Test
    void processPayment_failure() throws Exception {
        paymentRequest.setPaymentMethod("MOCK_CREDIT_CARD_FAIL"); // This method should always fail in service logic

        when(paymentRepository.findByOrderId(paymentRequest.getOrderId())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        doNothing().when(orderServiceClient).updateOrderStatus(anyLong(), anyString());

        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk()) // Still 200 OK, but status is FAILED
                .andExpect(jsonPath("$.status", is("FAILED")));

        verify(orderServiceClient).updateOrderStatus(paymentRequest.getOrderId(), "PAYMENT_FAILED");
        verify(paymentRepository, times(2)).save(any(Payment.class)); // PENDING then FAILED
    }


    @Test
    void processPayment_alreadySuccessful_conflict() throws Exception {
        Payment existingPayment = new Payment(1L, paymentRequest.getOrderId(), LocalDateTime.now(), paymentRequest.getAmount(), "MOCK_VISA_SUCCESS", "SUCCESSFUL");
        when(paymentRepository.findByOrderId(paymentRequest.getOrderId())).thenReturn(Optional.of(existingPayment));

        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isConflict()); // 409 Conflict

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderServiceClient, never()).updateOrderStatus(anyLong(), anyString());
    }
    
    @Test
    void processPayment_badRequest_missingOrderId() throws Exception {
        paymentRequest.setOrderId(null);
         mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getPaymentStatusByOrderId_found() throws Exception {
        Payment payment = new Payment(1L, 200L, LocalDateTime.now(), 150.0, "MOCK_MASTERCARD", "SUCCESSFUL");
        when(paymentRepository.findByOrderId(200L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/payments/order/{orderId}/status", 200L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(1)))
                .andExpect(jsonPath("$.orderId", is(200)))
                .andExpect(jsonPath("$.status", is("SUCCESSFUL")));
    }

    @Test
    void getPaymentStatusByOrderId_notFound() throws Exception {
        when(paymentRepository.findByOrderId(201L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/order/{orderId}/status", 201L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
