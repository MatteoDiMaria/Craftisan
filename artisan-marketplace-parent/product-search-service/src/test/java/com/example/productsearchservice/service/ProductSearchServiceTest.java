package com.example.productsearchservice.service;

import com.example.productsearchservice.dto.ProductResponse;
import com.example.productsearchservice.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;


import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductSearchServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("prod123");
        product.setName("Test Product");
        product.setCategory("Test Category");
        product.setPrice(50.0);
        product.setDescription("A test product description");
    }

    @Test
    void searchProducts_withQueryText() {
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products")))
                .thenReturn(Collections.singletonList(product));

        List<ProductResponse> responses = productSearchService.searchProducts("test", null, null, null);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class), eq("products"));
        Query capturedQuery = queryCaptor.getValue();
        assertNotNull(capturedQuery.getQueryObject().get("$text"));
        assertEquals("test", capturedQuery.getQueryObject().get("$text", org.bson.Document.class).getString("$search"));
    }

    @Test
    void searchProducts_withCategoryAndPriceRange() {
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products")))
                .thenReturn(Collections.singletonList(product));

        List<ProductResponse> responses = productSearchService.searchProducts(null, "Test Category", 20.0, 100.0);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Product.class), eq("products"));
        Query capturedQuery = queryCaptor.getValue();
        
        org.bson.Document queryObject = capturedQuery.getQueryObject();
        assertTrue(queryObject.containsKey("$and")); // Expecting an $and operator for multiple criteria
        
        @SuppressWarnings("unchecked") // Safe cast based on MongoDB query structure
        List<org.bson.Document> andClauses = (List<org.bson.Document>) queryObject.get("$and");
        
        boolean categoryFound = andClauses.stream().anyMatch(doc -> doc.containsKey("category") && doc.get("category").equals("Test Category"));
        boolean priceFound = andClauses.stream().anyMatch(doc -> doc.containsKey("price") && doc.get("price", org.bson.Document.class).get("$gte").equals(20.0) && doc.get("price", org.bson.Document.class).get("$lte").equals(100.0));

        assertTrue(categoryFound, "Category criteria not found or incorrect");
        assertTrue(priceFound, "Price criteria not found or incorrect");
    }
    
    @Test
    void searchProducts_noResults() {
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products")))
                .thenReturn(Collections.emptyList());
        
        List<ProductResponse> responses = productSearchService.searchProducts("nonexistent", null, null, null);
        
        assertTrue(responses.isEmpty());
    }
}
