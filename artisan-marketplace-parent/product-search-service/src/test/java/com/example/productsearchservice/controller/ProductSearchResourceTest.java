package com.example.productsearchservice.controller;

import com.example.productsearchservice.ProductSearchServiceApplication;
import com.example.productsearchservice.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = ProductSearchServiceApplication.class)
@AutoConfigureMockMvc
public class ProductSearchResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MongoTemplate mongoTemplate; // Mocking MongoTemplate for controller tests

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchProducts_withQueryText_success() throws Exception {
        Product product = new Product();
        product.setId("prod1");
        product.setName("Handmade Ceramic Mug");
        product.setDescription("A beautiful mug for your coffee");
        product.setCategory("Pottery");
        product.setPrice(25.00);

        List<Product> mockProducts = Collections.singletonList(product);
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products"))).thenReturn(mockProducts);

        mockMvc.perform(get("/api/search/products")
                .param("query", "mug")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("prod1")))
                .andExpect(jsonPath("$[0].name", is("Handmade Ceramic Mug")));
    }

    @Test
    void searchProducts_withCategoryAndPrice_success() throws Exception {
        Product product1 = new Product("prod1", "artisan1", "Silk Scarf", "Elegant silk scarf", 75.0, "Textiles", null, 10, null);
        Product product2 = new Product("prod2", "artisan1", "Linen Scarf", "Cool linen scarf", 60.0, "Textiles", null, 5, null);
        
        List<Product> mockProducts = List.of(product1, product2);
        // This mock is broad; for more precise testing, capture the query and assert its properties
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products"))).thenReturn(mockProducts);

        mockMvc.perform(get("/api/search/products")
                .param("category", "Textiles")
                .param("minPrice", "50")
                .param("maxPrice", "100")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("Textiles")))
                .andExpect(jsonPath("$[1].category", is("Textiles")));
    }
    
    @Test
    void searchProducts_noResults() throws Exception {
        when(mongoTemplate.find(any(Query.class), eq(Product.class), eq("products"))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search/products")
                .param("query", "nonexistentproductquery")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // TODO: Add tests for combined query parameters, edge cases for price, etc.
}
