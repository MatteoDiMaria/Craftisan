package com.example.productservice.service;

import com.example.productservice.dto.ProductCreateRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductCreateRequest createRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        createRequest = new ProductCreateRequest();
        createRequest.setArtisanId("artisan123");
        createRequest.setName("Handmade Mug");
        createRequest.setDescription("Beautifully crafted mug");
        createRequest.setPrice(25.99);
        createRequest.setCategory("Pottery");
        createRequest.setStockQuantity(10);
        createRequest.setImages(Collections.singletonList("image.url"));
        createRequest.setDetails(Collections.singletonMap("material", "ceramic"));

        product = new Product();
        BeanUtils.copyProperties(createRequest, product);
        product.setId("prod123");
    }

    @Test
    void createProduct_success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(createRequest);

        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(createRequest.getName(), response.getName());
        assertEquals(createRequest.getArtisanId(), response.getArtisanId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_success() {
        when(productRepository.findById("prod123")).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById("prod123");

        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById("unknownId")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById("unknownId");
        });
        assertEquals("Product not found with id: unknownId", exception.getMessage());
    }

    @Test
    void getProductsByArtisan_success() {
        when(productRepository.findByArtisanId("artisan123")).thenReturn(Collections.singletonList(product));

        List<ProductResponse> responses = productService.getProductsByArtisan("artisan123");

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(product.getId(), responses.get(0).getId());
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.existsById("prod123")).thenReturn(true);
        // No need to mock deleteById as it's a void method, just verify it's called
        
        productService.deleteProduct("prod123");
        
        verify(productRepository).deleteById("prod123");
    }

    @Test
    void deleteProduct_notFound() {
        when(productRepository.existsById("unknownId")).thenReturn(false);
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct("unknownId");
        });
        assertEquals("Product not found with id: unknownId", exception.getMessage());
    }

    // TODO: Add tests for updateProduct
}
