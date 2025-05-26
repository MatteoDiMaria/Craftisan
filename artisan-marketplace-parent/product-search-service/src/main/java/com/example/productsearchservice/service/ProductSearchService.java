package com.example.productsearchservice.service;

import com.example.productsearchservice.dto.ProductResponse;
import com.example.productsearchservice.model.Product;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ProductSearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ProductResponse> searchProducts(String queryText, String category, Double minPrice, Double maxPrice) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.hasText(queryText)) {
            // Using TextCriteria for full-text search.
            // Requires a text index on the fields you want to search (e.g., name, description, category).
            // Example: db.products.createIndex({ name: "text", description: "text", category: "text" })
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingAny(queryText);
            query.addCriteria(textCriteria);
        }

        if (StringUtils.hasText(category)) {
            criteriaList.add(Criteria.where("category").is(category));
        }

        if (minPrice != null && maxPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice).lte(maxPrice));
        } else if (minPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        } else if (maxPrice != null) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<Product> products = mongoTemplate.find(query, Product.class, "products");

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        BeanUtils.copyProperties(product, response);
        return response;
    }
}
