package com.example.productservice.controller;

import com.example.productservice.dto.ProductCreateRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.dto.ProductUpdateRequest;
import com.example.productservice.service.ProductService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/products") // Base path for this resource, will be prefixed by /api from Jersey config
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductService productService;

    @Autowired
    public ProductResource(ProductService productService) {
        this.productService = productService;
    }

    @POST
    public Response createProduct(ProductCreateRequest createRequest) {
        try {
            ProductResponse productResponse = productService.createProduct(createRequest);
            return Response.status(Response.Status.CREATED).entity(productResponse).build();
        } catch (Exception e) { // General exception handling, refine as needed
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{productId}")
    public Response updateProduct(@PathParam("productId") String productId, ProductUpdateRequest updateRequest) {
        try {
            ProductResponse productResponse = productService.updateProduct(productId, updateRequest);
            return Response.ok(productResponse).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{productId}")
    public Response getProductById(@PathParam("productId") String productId) {
        try {
            ProductResponse productResponse = productService.getProductById(productId);
            return Response.ok(productResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{productId}")
    public Response deleteProduct(@PathParam("productId") String productId) {
        try {
            productService.deleteProduct(productId);
            return Response.noContent().build(); // 204 No Content is typical for successful deletion
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/artisan/{artisanId}")
    public Response getProductsByArtisan(@PathParam("artisanId") String artisanId) {
        try {
            List<ProductResponse> products = productService.getProductsByArtisan(artisanId);
            return Response.ok(products).build();
        } catch (Exception e) { // General exception handling
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
