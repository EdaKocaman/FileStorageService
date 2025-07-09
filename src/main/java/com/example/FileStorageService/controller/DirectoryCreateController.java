package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.DirectoryDTO;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.exception.UserPermissionException;
import com.example.FileStorageService.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.*;

import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.DirectoryCreateService;
import com.example.FileStorageService.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/directories")
@Tag(name = "Directory Operations", description = "APIs for directory management")
@RequiredArgsConstructor
public class DirectoryCreateController {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryCreateController.class);
    private final DirectoryCreateService directoryCreateService;
    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @PostMapping("/create")
    @Operation(summary = "Create a new directory")
    public ResponseEntity<String> createDirectory(
            @RequestParam String name,
            @RequestParam (required = false)String path,
            @RequestParam(required = false) Long parentId) {
        try {
            User currentUser = userService.getCurrentUser();
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.CREATE_DIRECTORY);

            directoryCreateService.createDirectory(name, path, parentId, currentUser);
            logger.info("Directory created successfully: " + (path != null ? path + "/" : "") + name);
            return ResponseEntity.ok("Directory created successfully: " + (path != null ? path + "/" : "") + name);
        } catch (UserPermissionException e) {
            logger.error("Permission denied: " + e.getMessage());
            return ResponseEntity.status(403).body("Permission denied: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create directory: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to create directory: " + e.getMessage());
        }
    }

    @GetMapping("")
    @Operation(summary = "List all directories")
    public ResponseEntity<List<DirectoryDTO>> getAllDirectories() {

        try {
            User currentUser = userService.getCurrentUser();
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.LIST_DIRECTORIES);

            logger.info("Fetching all directories...");
            List<DirectoryDTO> directories = directoryCreateService.getAllDirectories();
            logger.info("Fetched {} directories.", directories.size());
            return ResponseEntity.ok(directories);
        } catch (HttpMessageNotWritableException ex) {
            logger.error("Error while writing response: {}", ex.getMessage());
            return ResponseEntity.status(500).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(null);
        }catch (Exception e) {
            logger.error("Error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/directories/{directoryId}")
    @Operation(summary = "Get a directory tree by ID")
    public ResponseEntity<DirectoryDTO> getDirectoryTree(@PathVariable Long directoryId) {
        try {
            User currentUser = userService.getCurrentUser();
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.VIEW_DIRECTORY);
            DirectoryDTO directory = directoryCreateService.getDirectoryTree(directoryId);
            if (directory == null) {
                return ResponseEntity.status(404).body(null);
            }
            return ResponseEntity.ok(directory);
        } catch (Exception e) {
            logger.error("Error fetching directory tree: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

} 