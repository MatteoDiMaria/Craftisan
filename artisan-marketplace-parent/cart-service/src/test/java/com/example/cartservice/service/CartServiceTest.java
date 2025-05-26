package com.example.cartservice.service;

import com.example.cartservice.dto.AddItemRequest;
import com.example.cartservice.dto.CartResponse;
import com.example.cartservice.model.Cart;
import com.example.cartservice.model.CartItem;
import com.example.cartservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private String testCartId = "user123";
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testCart = new Cart(testCartId);
        testCart.setLastModified(new Date());
    }

    @Test
    void getCart_existingCart() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        CartResponse response = cartService.getCart(testCartId);
        assertNotNull(response);
        assertEquals(testCartId, response.getCartId());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getCart_newCart() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());
        // findOrCreateCart will then call new Cart(cartId) which is implicitly tested
        // No need to mock save here as getCart for a new cart doesn't save it until an item is added.
        CartResponse response = cartService.getCart(testCartId);
        assertNotNull(response);
        assertEquals(testCartId, response.getCartId());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void addItemToCart_newItem() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setCurrentPrice(10.0);
        request.setProductName("Test Product");

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart)); // Cart exists
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItemToCart(testCartId, request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("prod1", response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(10.0, response.getItems().get(0).getPriceAtAddition());
        assertEquals(20.0, response.getProvisionalTotal());
        verify(cartRepository).save(testCart);
    }

    @Test
    void addItemToCart_existingItem() {
        CartItem existingItem = new CartItem("prod1", 1, 10.0, "Test Product", null);
        testCart.getItems().add(existingItem);

        AddItemRequest request = new AddItemRequest();
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setCurrentPrice(10.0); // Price might have changed, but we use priceAtAddition from original
        request.setProductName("Test Product");


        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItemToCart(testCartId, request);

        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getItems().get(0).getQuantity()); // 1 existing + 2 new
        assertEquals(10.0, response.getItems().get(0).getPriceAtAddition()); // Should remain original price
        assertEquals(30.0, response.getProvisionalTotal());
    }

    @Test
    void removeItemFromCart_itemExists() {
        CartItem itemToRemove = new CartItem("prod1", 2, 10.0, "Test Product", null);
        testCart.getItems().add(itemToRemove);
        testCart.getItems().add(new CartItem("prod2", 1, 5.0, "Another Product", null));

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.removeItemFromCart(testCartId, "prod1");

        assertEquals(1, response.getItems().size());
        assertEquals("prod2", response.getItems().get(0).getProductId());
        assertEquals(5.0, response.getProvisionalTotal());
    }

    @Test
    void updateItemQuantity_itemExists_updateQuantity() {
        CartItem itemToUpdate = new CartItem("prod1", 2, 10.0, "Test Product", null);
        testCart.getItems().add(itemToUpdate);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.updateItemQuantity(testCartId, "prod1", 5);

        assertEquals(1, response.getItems().size());
        assertEquals(5, response.getItems().get(0).getQuantity());
        assertEquals(50.0, response.getProvisionalTotal());
    }

    @Test
    void updateItemQuantity_itemExists_removeIfQuantityZero() {
        CartItem itemToUpdate = new CartItem("prod1", 2, 10.0, "Test Product", null);
        testCart.getItems().add(itemToUpdate);

        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.updateItemQuantity(testCartId, "prod1", 0);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0.0, response.getProvisionalTotal());
    }
    
    @Test
    void updateItemQuantity_itemNotFound_throwsException() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        // No save mock needed as it should throw before saving

        Exception exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateItemQuantity(testCartId, "nonExistentProd", 1);
        });
        assertEquals("Item not found in cart: nonExistentProd", exception.getMessage());
    }


    @Test
    void clearCart_cartExists() {
        testCart.getItems().add(new CartItem("prod1", 2, 10.0, "Test Product", null));
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.clearCart(testCartId);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0.0, response.getProvisionalTotal());
    }
}
