package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setUserId("user123");
        orderRequest.setShippingAddress("123 Main St, Anytown, USA");

        OrderItemRequest itemRequest1 = new OrderItemRequest();
        itemRequest1.setProductId("prod1");
        itemRequest1.setQuantity(2);
        itemRequest1.setPricePerItem(10.0);
        itemRequest1.setProductName("Product 1");

        orderRequest.setItems(Collections.singletonList(itemRequest1));

        order = new Order();
        order.setId(1L);
        order.setUserId(orderRequest.getUserId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING_PAYMENT");
        order.setTotalAmount(20.0); // 2 * 10.0

        OrderItem orderItem = new OrderItem();
        orderItem.setId(101L);
        orderItem.setProductId(itemRequest1.getProductId());
        orderItem.setQuantity(itemRequest1.getQuantity());
        orderItem.setPricePerItem(itemRequest1.getPricePerItem());
        orderItem.setProductName(itemRequest1.getProductName());
        order.addItem(orderItem);
    }

    @Test
    void createOrder_success() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals(order.getId(), response.getOrderId());
        assertEquals("PENDING_PAYMENT", response.getStatus());
        assertEquals(20.0, response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals("prod1", response.getItems().get(0).getProductId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(order.getId(), response.getOrderId());
        assertEquals(order.getUserId(), response.getUserId());
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(2L);
        });
        assertEquals("Order not found with id: 2", exception.getMessage());
    }

    @Test
    void getOrdersByUserId_success() {
        when(orderRepository.findByUserId("user123")).thenReturn(Collections.singletonList(order));

        List<OrderResponse> responses = orderService.getOrdersByUserId("user123");

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(order.getId(), responses.get(0).getOrderId());
    }

    @Test
    void updateOrderStatus_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        // For the updated order, create a new instance or modify 'order' then return it
        Order updatedOrder = new Order();
        BeanUtils.copyProperties(order, updatedOrder); // Using springframework BeanUtils
        updatedOrder.setStatus("PAID");
        
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, "PAID");

        assertNotNull(response);
        assertEquals("PAID", response.getStatus());
        assertEquals(order.getId(), response.getOrderId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_notFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus(2L, "PAID");
        });
        assertEquals("Order not found with id: 2", exception.getMessage());
    }
}
