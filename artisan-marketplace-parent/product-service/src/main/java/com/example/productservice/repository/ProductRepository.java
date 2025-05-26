package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByArtisanId(String artisanId);
    List<Product> findByCategory(String category); // Example of another useful query
}
