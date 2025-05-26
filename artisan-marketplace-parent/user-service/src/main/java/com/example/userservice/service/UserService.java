package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists"); // Consider a custom exception
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail())); // Consider a custom exception

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password"); // Consider a custom exception
        }

        // Placeholder for JWT generation
        String token = "dummy-jwt-token-for-" + user.getEmail(); 

        return new AuthResponse(token, mapToUserResponse(user));
    }

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    // For updateUserProfile, we'll need a UserUpdateRequest DTO.
    // For now, let's assume it's similar to UserRegistrationRequest but fields are optional.
    // Or we can reuse UserRegistrationRequest if all fields are expected for an update.
    // Let's create a simple UserUpdateRequest DTO for clarity.
    public UserResponse updateUserProfile(Long userId, UserRegistrationRequest requestData) { // Using UserRegistrationRequest for now
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update fields if provided in requestData
        if (requestData.getEmail() != null && !requestData.getEmail().isEmpty()) {
            // Check if new email is already taken by another user
            Optional<User> existingUserWithNewEmail = userRepository.findByEmail(requestData.getEmail());
            if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(userId)) {
                throw new RuntimeException("Email already taken");
            }
            user.setEmail(requestData.getEmail());
        }
        if (requestData.getFirstName() != null && !requestData.getFirstName().isEmpty()) {
            user.setFirstName(requestData.getFirstName());
        }
        if (requestData.getLastName() != null && !requestData.getLastName().isEmpty()) {
            user.setLastName(requestData.getLastName());
        }
        // Password update should ideally be a separate endpoint and flow for security reasons
        // For now, if password is provided in this request, we update it.
        if (requestData.getPassword() != null && !requestData.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestData.getPassword()));
        }
        if (requestData.getRole() != null && !requestData.getRole().isEmpty()) {
            user.setRole(requestData.getRole());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }


    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }
}
