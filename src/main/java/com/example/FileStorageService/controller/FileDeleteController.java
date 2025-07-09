package com.example.FileStorageService.controller;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.example.FileStorageService.Interface.FileUploadServiceInterface;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "File Operations")
@RequiredArgsConstructor
public class FileDeleteController {
    private static final Logger logger = LoggerFactory.getLogger(FileDeleteController.class);
    private final FileUploadServiceInterface deleteFileService;
    private final UserService userService;
    private final UserPermissionService permissionService;

    @Operation(summary = "Delete a file by ID")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<String> deleteFileById(@PathVariable Long fileId, @RequestParam("userUuid") UUID userUuid) {
        try {
            User currentUser = userService.getUserByUuid(userUuid);
            permissionService.checkPermission(currentUser.getUserUuid(), Roles.DELETE_FILE);

            logger.info("Deleting file with ID: {}", fileId);
            deleteFileService.deleteFileID(fileId);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all files")
    @GetMapping("/files/all")
    public ResponseEntity<?> getFiles() {
        logger.info("Get all files");
        return ResponseEntity.ok(deleteFileService.getAllFiles());
    }
}