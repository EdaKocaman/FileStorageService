package com.example.FileStorageService.service;

import org.springframework.stereotype.Service;

import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String password) {
        logger.info("Creating new user with username: {}", username);
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID user_uuid) {
        logger.debug("Fetching user with ID: {}", user_uuid);
        User user = userRepository.findByUserUuid(user_uuid)
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + user_uuid));
        return user;
    }

    public User getCurrentUser() {
        return userRepository.findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No users found in database"));
    }
    public boolean isValidUser(String authToken) {
        return authToken != null && authToken.equals("valid_token");
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findUserByUuid(UUID user_uuid) {
        return userRepository.findByUserUuid(user_uuid)
                .orElseThrow(() -> new RuntimeException("User not found with UUID: " + user_uuid));
    }
    public User getUserByUuid(UUID userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}