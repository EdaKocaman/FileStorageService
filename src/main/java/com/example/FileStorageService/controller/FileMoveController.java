package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.FileMoveRequest;
import com.example.FileStorageService.service.FileMoveService;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Operations", description = "APIs for move file")
@RequiredArgsConstructor
public class FileMoveController {
    private static final Logger logger = LoggerFactory.getLogger(FileMoveController.class);
    private final FileMoveService fileMoveService;
    private final UserPermissionService userPermissionService;
    private final UserService userService;

    @PostMapping("/move")
    @Operation(summary = "Move file")
    public ResponseEntity<String> moveFile(@RequestBody FileMoveRequest fileMoveRequest) {
        try {
            User currentUser = userService.getUserByUuid(fileMoveRequest.getUserUuid());
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.MOVE_FILE);
            fileMoveService.moveFile(fileMoveRequest.getFileGuid(), fileMoveRequest.getTargetDirectoryID(), fileMoveRequest.getUserUuid());
            logger.info("File {} moved successfully to directory {}", fileMoveRequest.getFileGuid(), fileMoveRequest.getTargetDirectoryID());
            return ResponseEntity.ok("File moved successfully!");
        } catch (RuntimeException e) {
            logger.error("File move failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while moving file: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Unexpected error occurred.");
        }
    }
}