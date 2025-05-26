package com.example.cartservice.service;

import com.example.cartservice.dto.AddItemRequest;
import com.example.cartservice.dto.CartItemResponse;
import com.example.cartservice.dto.CartResponse;
import com.example.cartservice.model.Cart;
import com.example.cartservice.model.CartItem;
import com.example.cartservice.repository.CartRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartResponse getCart(String cartId) {
        Cart cart = findOrCreateCart(cartId);
        return mapToCartResponse(cart);
    }

    public CartResponse addItemToCart(String cartId, AddItemRequest itemRequest) {
        Cart cart = findOrCreateCart(cartId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(itemRequest.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
            // Optionally, update priceAtAddition if business logic dictates (e.g., if price changed since last addition)
            // For this implementation, priceAtAddition is set at first add.
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(itemRequest.getProductId());
            newItem.setQuantity(itemRequest.getQuantity());
            newItem.setPriceAtAddition(itemRequest.getCurrentPrice()); // Price when first added
            newItem.setProductName(itemRequest.getProductName());
            newItem.setProductImage(itemRequest.getProductImage());
            cart.getItems().add(newItem);
        }

        cart.setLastModified(new Date());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public CartResponse removeItemFromCart(String cartId, String productId) {
        Cart cart = findCartOrThrow(cartId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.setLastModified(new Date());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public CartResponse updateItemQuantity(String cartId, String productId, int quantity) {
        Cart cart = findCartOrThrow(cartId);

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (quantity <= 0) {
                cart.getItems().remove(item);
            } else {
                item.setQuantity(quantity);
            }
        } else {
            throw new RuntimeException("Item not found in cart: " + productId); // Consider custom exception
        }

        cart.setLastModified(new Date());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    public CartResponse clearCart(String cartId) {
        Cart cart = findCartOrThrow(cartId);
        cart.getItems().clear();
        cart.setLastModified(new Date());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    private Cart findOrCreateCart(String cartId) {
        return cartRepository.findById(cartId).orElseGet(() -> new Cart(cartId));
    }

    private Cart findCartOrThrow(String cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found: " + cartId)); // Consider custom exception
    }

    private double calculateProvisionalTotal(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getPriceAtAddition() * item.getQuantity())
                .sum();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        response.setProvisionalTotal(calculateProvisionalTotal(cart));
        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse itemResponse = new CartItemResponse();
        BeanUtils.copyProperties(item, itemResponse);
        itemResponse.setItemTotal(item.getPriceAtAddition() * item.getQuantity());
        return itemResponse;
    }
}
