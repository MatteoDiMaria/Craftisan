package com.example.userservice.controller;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/register")
    public Response registerUser(UserRegistrationRequest registrationRequest) {
        try {
            UserResponse userResponse = userService.registerUser(registrationRequest);
            return Response.status(Response.Status.CREATED).entity(userResponse).build();
        } catch (RuntimeException e) {
            // Consider more specific error handling and response codes
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    public Response loginUser(LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.loginUser(loginRequest);
            return Response.ok(authResponse).build();
        } catch (RuntimeException e) {
            // Consider more specific error handling and response codes
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{userId}/profile")
    public Response getUserProfile(@PathParam("userId") Long userId) {
        try {
            UserResponse userResponse = userService.getUserProfile(userId);
            return Response.ok(userResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{userId}/profile")
    public Response updateUserProfile(@PathParam("userId") Long userId, UserRegistrationRequest updateRequest) {
        // For now, using UserRegistrationRequest as the DTO for updates.
        // A more specific UserUpdateRequestDTO would be better in a real application.
        try {
            UserResponse userResponse = userService.updateUserProfile(userId, updateRequest);
            return Response.ok(userResponse).build();
        } catch (RuntimeException e) {
            // Consider more specific error handling and response codes
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
