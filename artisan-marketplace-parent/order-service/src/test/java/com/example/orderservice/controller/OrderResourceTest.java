package com.example.orderservice.controller;

import com.example.orderservice.OrderServiceApplication;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(classes = OrderServiceApplication.class)
@AutoConfigureMockMvc
public class OrderResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository; // Mock repository for controller tests

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setUserId("userTest1");
        orderRequest.setShippingAddress("123 Test St, Testville");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId("prodTest1");
        itemRequest.setQuantity(1);
        itemRequest.setPricePerItem(50.0);
        itemRequest.setProductName("Test Product 1");
        orderRequest.setItems(Collections.singletonList(itemRequest));

        order = new Order();
        order.setId(1L);
        order.setUserId(orderRequest.getUserId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING_PAYMENT");
        order.setTotalAmount(50.0);
        // Note: In a real scenario, OrderItems would also be part of the 'order' object for findById etc.
    }

    @AfterEach
    void tearDown() {
        // Clean up if using an embedded DB that's not fully reset.
        // For @MockBean, this is less critical for repository state.
    }

    @Test
    void createOrder_success() throws Exception {
        // Mock the behavior of orderRepository.save()
        // The service will construct an Order entity, and this mock will return it (or a version of it)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L); // Simulate ID generation
            savedOrder.setOrderDate(LocalDateTime.now()); // Simulate @PrePersist
            // Ensure items are part of the savedOrder if the service adds them before save
            // and the mapping relies on them for the response.
            // Our service maps from the returned savedOrder.
            return savedOrder;
        });

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.userId", is(orderRequest.getUserId())))
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.totalAmount", is(50.0)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is("prodTest1")));
    }

    @Test
    void getOrderById_success() throws Exception {
        // Need to mock findById to return the 'order' object, including its items for the response mapping
        com.example.orderservice.model.OrderItem orderItem = new com.example.orderservice.model.OrderItem();
        orderItem.setId(10L);
        orderItem.setProductId("prodTest1");
        orderItem.setQuantity(1);
        orderItem.setPricePerItem(50.0);
        orderItem.setProductName("Test Product 1");
        order.setItems(Collections.singletonList(orderItem)); // Add items to the order being returned by mock

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/{orderId}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.userId", is(order.getUserId())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is("prodTest1")));
    }

    @Test
    void getOrderById_notFound() throws Exception {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/{orderId}", 2L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByUserId_success() throws Exception {
        // Similar to getOrderById_success, ensure items are populated in the mock Order object
        com.example.orderservice.model.OrderItem orderItem = new com.example.orderservice.model.OrderItem();
        orderItem.setProductId("prodTest1"); // ... set other fields
        order.setItems(Collections.singletonList(orderItem));

        when(orderRepository.findByUserId("userTest1")).thenReturn(Collections.singletonList(order));

        mockMvc.perform(get("/api/orders/user/{userId}", "userTest1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(1)))
                .andExpect(jsonPath("$[0].userId", is("userTest1")));
    }
    
    @Test
    void getOrdersByUserId_notFound() throws Exception {
        when(orderRepository.findByUserId("userNonExistent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/user/{userId}", "userNonExistent")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderStatus_success() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        Order updatedOrder = new Order();
        org.springframework.beans.BeanUtils.copyProperties(order, updatedOrder);
        updatedOrder.setStatus("PAID"); // The status to be updated to
        
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/orders/{orderId}/status", 1L)
                .param("status", "PAID")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.status", is("PAID")));
    }
    
    @Test
    void updateOrderStatus_missingStatusParam() throws Exception {
        mockMvc.perform(put("/api/orders/{orderId}/status", 1L)
                // No status query parameter
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // TODO: Add tests for other scenarios, e.g., order not found for status update
}
