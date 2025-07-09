package com.example.FileStorageService.controller;

import com.example.FileStorageService.model.Role;
import com.example.FileStorageService.model.UserRole;
import com.example.FileStorageService.repository.UserRoleRepository;
import com.example.FileStorageService.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.UserService;
import com.example.FileStorageService.enums.Roles;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User Operations", description = "APIs for user management")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionService permissionService;


    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<?> createUser(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam UUID requesterUuid) {
        try {
            permissionService.checkPermission(requesterUuid, Roles.CREATE_USER);

            logger.info("Creating user with username: {}", username);
            User user = userService.createUser(username, password);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error listing users: " + e.getMessage());
        }
    }

    @GetMapping("/{user_uuid}/roles")
    @Operation(summary = "Get roles by user UUID")
    public ResponseEntity<List<Role>> getUserRolesByUuid(@PathVariable UUID user_uuid) {
        try {
            List<UserRole> userRoles = userRoleRepository.findAllByUser_UserUuid(user_uuid);

            if (userRoles.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            }

            List<Role> roles = userRoles.stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error getting roles for user with UUID: {}", user_uuid);
            return ResponseEntity.status(500).body(null);
        }
    }
} 