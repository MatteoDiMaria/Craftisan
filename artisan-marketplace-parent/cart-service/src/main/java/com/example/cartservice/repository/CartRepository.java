package com.example.cartservice.repository;

import com.example.cartservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// No need for Optional<Cart> findByUserId(String userId) if Cart's ID *is* the userId.
// MongoRepository<Cart, String> already provides findById(String id).
@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
}
