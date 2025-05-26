package com.example.productservice.service;

import com.example.productservice.dto.ProductCreateRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.dto.ProductUpdateRequest;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        // Any specific logic for conversion, e.g. if artisanId needs type conversion
        // or if there are default values to set.

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(String productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId)); // Consider custom exception

        // Update fields only if they are provided in the request
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (StringUtils.hasText(request.getCategory())) {
            product.setCategory(request.getCategory());
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            product.setImages(request.getImages());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            // For map, decide if you want to merge or replace. This replaces.
            product.setDetails(request.getDetails());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId)); // Consider custom exception
        return mapToProductResponse(product);
    }

    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId); // Consider custom exception
        }
        productRepository.deleteById(productId);
    }

    public List<ProductResponse> getProductsByArtisan(String artisanId) {
        List<Product> products = productRepository.findByArtisanId(artisanId);
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
