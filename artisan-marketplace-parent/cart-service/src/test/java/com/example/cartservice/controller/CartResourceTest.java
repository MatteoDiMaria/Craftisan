package com.example.cartservice.controller;

import com.example.cartservice.CartServiceApplication;
import com.example.cartservice.dto.AddItemRequest;
import com.example.cartservice.model.Cart;
import com.example.cartservice.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = CartServiceApplication.class)
@AutoConfigureMockMvc
public class CartResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartRepository cartRepository; // Mocking repository for controller tests

    @Autowired
    private ObjectMapper objectMapper;

    private final String testCartId = "userTestCart1";

    @AfterEach
    void tearDown() {
        // If using an embedded MongoDB for testing, might clear it here.
        // With @MockBean, repository is mocked, so no direct DB interaction from tests.
    }

    @Test
    void getCart_existingCart() throws Exception {
        Cart cart = new Cart(testCartId);
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(cart));

        mockMvc.perform(get("/api/carts/{cartId}", testCartId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(testCartId)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getCart_newCart_shouldCreateAndReturn() throws Exception {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());
        // The service's getCart method should handle creation if not found.
        // If it saves immediately, mock save. If it only creates in memory until item add, this is fine.
        // Our CartService.getCart (findOrCreateCart) does not save on get if new, only upon modification.

        mockMvc.perform(get("/api/carts/{cartId}", testCartId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(testCartId)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void addItemToCart_success() throws Exception {
        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId("prod100");
        addItemRequest.setQuantity(2);
        addItemRequest.setProductName("Test Item");
        addItemRequest.setCurrentPrice(15.50);

        Cart cart = new Cart(testCartId); // Simulate cart being fetched or created
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId", is(testCartId)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is("prod100")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.provisionalTotal", is(31.0)));
    }

    @Test
    void removeItemFromCart_success() throws Exception {
        Cart cart = new Cart(testCartId);
        cart.getItems().add(new com.example.cartservice.model.CartItem("prodToRemove", 1, 10.0, "To Remove", null));
        cart.getItems().add(new com.example.cartservice.model.CartItem("prodToKeep", 1, 20.0, "To Keep", null));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            // Simulate removal for correct total calculation in response
            savedCart.getItems().removeIf(item -> item.getProductId().equals("prodToRemove"));
            return savedCart;
        });

        mockMvc.perform(delete("/api/carts/{cartId}/items/{productId}", testCartId, "prodToRemove")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is("prodToKeep")))
                .andExpect(jsonPath("$.provisionalTotal", is(20.0)));
    }

    @Test
    void updateItemQuantity_success() throws Exception {
        Cart cart = new Cart(testCartId);
        cart.getItems().add(new com.example.cartservice.model.CartItem("prodToUpdate", 1, 10.0, "To Update", null));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));


        mockMvc.perform(put("/api/carts/{cartId}/items/{productId}", testCartId, "prodToUpdate")
                .param("quantity", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(5)))
                .andExpect(jsonPath("$.provisionalTotal", is(50.0)));
    }
    
    @Test
    void updateItemQuantity_invalidQuantity_badRequest() throws Exception {
        mockMvc.perform(put("/api/carts/{cartId}/items/{productId}", testCartId, "prodToUpdate")
                .param("quantity", "-1") // Invalid quantity
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clearCart_success() throws Exception {
        Cart cart = new Cart(testCartId);
        cart.getItems().add(new com.example.cartservice.model.CartItem("prod1", 1, 10.0, "Item 1", null));
        cart.getItems().add(new com.example.cartservice.model.CartItem("prod2", 1, 20.0, "Item 2", null));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.getItems().clear(); // Simulate clearing for correct total
            return savedCart;
        });

        mockMvc.perform(delete("/api/carts/{cartId}", testCartId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.provisionalTotal", is(0.0)));
    }

    // TODO: Add tests for edge cases like item not found for update/delete, cart not found etc.
}
