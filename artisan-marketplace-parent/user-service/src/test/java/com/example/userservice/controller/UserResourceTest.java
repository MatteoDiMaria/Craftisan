package com.example.userservice.controller;

import com.example.userservice.UserServiceApplication;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
public class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository; // Mock repository to avoid DB interaction in this test

    @Autowired
    private PasswordEncoder passwordEncoder; // Autowire the actual password encoder

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings

    @BeforeEach
    void setUp() {
        // Clear any existing users or specific setup for each test if needed
        // For example, ensure no user exists with the test email before a registration test
        // when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void registerUser_success() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole("CUSTOMER");

        // Mock repository behavior
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        savedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        savedUser.setFirstName(request.getFirstName());
        savedUser.setLastName(request.getLastName());
        savedUser.setRole(request.getRole());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        MvcResult result = mockMvc.perform(post("/api/users/register") // Note: /api is from spring.jersey.application-path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // JAX-RS returns 201 Created
                .andExpect(jsonPath("$.email", is(request.getEmail())))
                .andExpect(jsonPath("$.firstName", is(request.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(request.getLastName())))
                .andExpect(jsonPath("$.role", is(request.getRole())))
                .andReturn();

        // You can further inspect the response if needed:
        // String responseBody = result.getResponse().getContentAsString();
        // UserResponse userResponse = objectMapper.readValue(responseBody, UserResponse.class);
        // assertNotNull(userResponse.getId());
    }

    @Test
    void registerUser_emailExists() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existinguser@example.com");
        request.setPassword("password123");
        request.setFirstName("Existing");
        request.setLastName("User");
        request.setRole("CUSTOMER");

        // Mock repository to simulate email already existing
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // JAX-RS returns 400 Bad Request
    }

    // TODO: Add more integration tests for login, getProfile, updateProfile
}
