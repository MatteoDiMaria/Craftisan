package com.example.cartservice.controller;

import com.example.cartservice.dto.AddItemRequest;
import com.example.cartservice.dto.CartResponse;
import com.example.cartservice.service.CartService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/carts") // Base path will be /api/carts
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    private final CartService cartService;

    @Autowired
    public CartResource(CartService cartService) {
        this.cartService = cartService;
    }

    @GET
    @Path("/{cartId}")
    public Response getCart(@PathParam("cartId") String cartId) {
        try {
            CartResponse cartResponse = cartService.getCart(cartId);
            return Response.ok(cartResponse).build();
        } catch (RuntimeException e) {
            // Assuming CartNotFoundException or similar might be thrown
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{cartId}/items")
    public Response addItemToCart(@PathParam("cartId") String cartId, AddItemRequest addItemRequest) {
        try {
            CartResponse cartResponse = cartService.addItemToCart(cartId, addItemRequest);
            return Response.ok(cartResponse).build();
        } catch (RuntimeException e) { // Catch specific exceptions like ProductNotFound if relevant
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{cartId}/items/{productId}")
    public Response removeItemFromCart(@PathParam("cartId") String cartId, @PathParam("productId") String productId) {
        try {
            CartResponse cartResponse = cartService.removeItemFromCart(cartId, productId);
            return Response.ok(cartResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{cartId}/items/{productId}")
    public Response updateItemQuantity(@PathParam("cartId") String cartId,
                                       @PathParam("productId") String productId,
                                       @QueryParam("quantity") int quantity) {
        if (quantity < 0) { // Basic validation
             return Response.status(Response.Status.BAD_REQUEST).entity("Quantity cannot be negative.").build();
        }
        try {
            CartResponse cartResponse = cartService.updateItemQuantity(cartId, productId, quantity);
            return Response.ok(cartResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{cartId}")
    public Response clearCart(@PathParam("cartId") String cartId) {
        try {
            CartResponse cartResponse = cartService.clearCart(cartId);
            return Response.ok(cartResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
