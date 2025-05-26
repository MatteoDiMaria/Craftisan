package com.example.productsearchservice.controller;

import com.example.productsearchservice.dto.ProductResponse;
import com.example.productsearchservice.service.ProductSearchService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/search/products") // Base path for this resource, will be prefixed by /api from Jersey config
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Though GET usually doesn't consume, good practice to specify
public class ProductSearchResource {

    private final ProductSearchService productSearchService;

    @Autowired
    public ProductSearchResource(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GET
    public Response searchProducts(
            @QueryParam("query") String query,
            @QueryParam("category") String category,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice) {
        try {
            List<ProductResponse> products = productSearchService.searchProducts(query, category, minPrice, maxPrice);
            if (products.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No products found matching your criteria.").build();
            }
            return Response.ok(products).build();
        } catch (Exception e) {
            // Log the exception e.g. e.printStackTrace(); or use a logger
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error during product search: " + e.getMessage()).build();
        }
    }
}
