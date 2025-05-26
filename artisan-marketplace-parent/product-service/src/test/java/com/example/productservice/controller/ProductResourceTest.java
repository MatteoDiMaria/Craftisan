package com.example.productservice.controller;

import com.example.productservice.ProductServiceApplication;
import com.example.productservice.dto.ProductCreateRequest;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = ProductServiceApplication.class)
@AutoConfigureMockMvc
public class ProductResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository; // Mock repository

    @Autowired
    private ObjectMapper objectMapper;

    // Optional: If you need to interact with MongoDB directly for setup/teardown in a more complex test
    // @Autowired
    // private MongoTemplate mongoTemplate;

    @AfterEach
    void tearDown() {
        // Clean up database if using an embedded or test-specific database and not fully mocking.
        // For @MockBean setup, this might not be necessary as the actual DB isn't hit.
    }

    @Test
    void createProduct_success() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setArtisanId("artisanTestId");
        request.setName("Test Product");
        request.setDescription("A product for testing");
        request.setPrice(19.99);
        request.setCategory("Test Category");
        request.setStockQuantity(100);
        request.setImages(Collections.singletonList("test.jpg"));
        request.setDetails(Collections.singletonMap("key", "value"));

        Product savedProduct = new Product();
        savedProduct.setId("mockProductId");
        savedProduct.setArtisanId(request.getArtisanId());
        savedProduct.setName(request.getName());
        savedProduct.setDescription(request.getDescription());
        savedProduct.setPrice(request.getPrice());
        savedProduct.setCategory(request.getCategory());
        savedProduct.setStockQuantity(request.getStockQuantity());
        savedProduct.setImages(request.getImages());
        savedProduct.setDetails(request.getDetails());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/products") // Jersey path defined in properties + controller
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("mockProductId")))
                .andExpect(jsonPath("$.name", is(request.getName())))
                .andExpect(jsonPath("$.artisanId", is(request.getArtisanId())));
    }

    @Test
    void getProductById_success() throws Exception {
        Product product = new Product("prod123", "artisanX", "Cool Product", "Very cool", 50.0, "Gadgets", null, 20, null);
        when(productRepository.findById("prod123")).thenReturn(java.util.Optional.of(product));

        mockMvc.perform(get("/api/products/prod123")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("prod123")))
                .andExpect(jsonPath("$.name", is("Cool Product")));
    }
    
    @Test
    void getProductById_notFound() throws Exception {
        when(productRepository.findById("nonExistentId")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/products/nonExistentId")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // TODO: Add more integration tests for update, delete, getByArtisanId
}
