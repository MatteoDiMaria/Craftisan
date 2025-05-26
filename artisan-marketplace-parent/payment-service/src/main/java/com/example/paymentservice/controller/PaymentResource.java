package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/payments") // Base path will be /api/payments after Jersey config
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private final PaymentService paymentService;

    @Autowired
    public PaymentResource(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @POST
    @Path("/process")
    public Response processPayment(PaymentRequest paymentRequest) {
        if (paymentRequest.getOrderId() == null || paymentRequest.getAmount() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Order ID and positive amount are required.")
                           .build();
        }
        try {
            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);
            if ("SUCCESSFUL".equals(paymentResponse.getStatus())) {
                return Response.ok(paymentResponse).build();
            } else {
                // Could also return 200 OK with a FAILED status, or a more specific error code
                // For now, let's assume 200 OK for both successful and failed payment processing attempts
                return Response.ok(paymentResponse).build(); 
            }
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build(); // e.g. payment already processed
        } catch (Exception e) {
            // Log e
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error processing payment: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/order/{orderId}/status")
    public Response getPaymentStatusByOrderId(@PathParam("orderId") Long orderId) {
        try {
            PaymentResponse paymentResponse = paymentService.getPaymentStatusByOrderId(orderId);
            return Response.ok(paymentResponse).build();
        } catch (RuntimeException e) {
            // Typically, if payment not found, means it hasn't been processed or orderId is wrong
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
