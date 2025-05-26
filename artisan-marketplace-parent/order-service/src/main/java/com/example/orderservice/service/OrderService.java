package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    // private final OrderItemRepository orderItemRepository; // Not strictly needed if cascading

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional // Ensure all operations are part of a single transaction
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setStatus("PENDING_PAYMENT"); // Initial status
        order.setOrderDate(LocalDateTime.now()); // Set by @PrePersist, but can be explicit

        double totalAmount = 0;
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPricePerItem(itemRequest.getPricePerItem());
            orderItem.setProductName(itemRequest.getProductName());
            order.addItem(orderItem); // This also sets orderItem.setOrder(order)
            totalAmount += itemRequest.getPricePerItem() * itemRequest.getQuantity();
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        
        // TODO: Future - Trigger cart clearing (e.g., call Cart service or publish event)
        
        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId)); // Consider custom exception
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId)); // Consider custom exception
        
        // Add business logic for valid status transitions if needed
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse itemResponse = new OrderItemResponse();
        itemResponse.setId(item.getId());
        itemResponse.setProductId(item.getProductId());
        itemResponse.setQuantity(item.getQuantity());
        itemResponse.setPricePerItem(item.getPricePerItem());
        itemResponse.setProductName(item.getProductName());
        itemResponse.setItemTotal(item.getPricePerItem() * item.getQuantity());
        return itemResponse;
    }
}
