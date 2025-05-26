package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.OrderService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/orders") // Base path will be /api/orders after Jersey config
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService;

    @Autowired
    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @POST
    public Response createOrder(OrderRequest orderRequest) {
        try {
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            return Response.status(Response.Status.CREATED).entity(orderResponse).build();
        } catch (Exception e) { // General exception, refine as needed
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{orderId}")
    public Response getOrderById(@PathParam("orderId") Long orderId) {
        try {
            OrderResponse orderResponse = orderService.getOrderById(orderId);
            return Response.ok(orderResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/user/{userId}")
    public Response getOrdersByUserId(@PathParam("userId") String userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            if (orders.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No orders found for user: " + userId).build();
            }
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{orderId}/status")
    public Response updateOrderStatus(@PathParam("orderId") Long orderId, @QueryParam("status") String status) {
        if (status == null || status.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Status query parameter is required.").build();
        }
        try {
            OrderResponse orderResponse = orderService.updateOrderStatus(orderId, status);
            return Response.ok(orderResponse).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
