package com.example.userservice.service;

import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // We need a real PasswordEncoder here for the test, or mock its behavior.
    // For simplicity, let's use a real one.
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Re-initialize userService with the real passwordEncoder if it's not injected properly
        // or if constructor injection is preferred for PasswordEncoder.
        // However, InjectMocks should handle the userRepository.
        // If passwordEncoder was @Autowired in UserService, this setup would be different.
        // For this structure, let's ensure UserService gets the encoder.
        // One way: userService = new UserService(userRepository, passwordEncoder);
        // Or, ensure the field in UserService is not final if not using constructor injection for it,
        // or use reflection to set it.
        // The current UserService uses constructor injection for both, so @InjectMocks should work
        // if PasswordEncoder is also a @Mock or a real instance provided.
        // Let's adjust UserService to accept PasswordEncoder via constructor for clarity in testing.
        // (Verified UserService already uses constructor injection for PasswordEncoder)
    }

    @Test
    void registerUser_success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole("CUSTOMER");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        savedUser.setPassword(passwordEncoder.encode(request.getPassword())); // Ensure password in mock is encoded
        savedUser.setFirstName(request.getFirstName());
        savedUser.setLastName(request.getLastName());
        savedUser.setRole(request.getRole());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Manually create UserService with mocks if @InjectMocks and constructor injection for PasswordEncoder has issues.
        // This ensures passwordEncoder is correctly used.
        userService = new UserService(userRepository, passwordEncoder);


        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
        assertEquals(request.getRole(), response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    void registerUser_emailExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole("CUSTOMER");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        
        // Ensure userService is initialized if not using @InjectMocks properly for all dependencies
        userService = new UserService(userRepository, passwordEncoder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    // TODO: Add tests for loginUser, getUserProfile, updateUserProfile
}
