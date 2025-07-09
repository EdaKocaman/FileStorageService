package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.DirectoryCopyRequest;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.DirectoryCopyService;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directories")
@Tag(name = "Directory Operations")
public class DirectoryCopyController {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryCopyController.class);

    private final DirectoryCopyService directoryCopyService;
    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @PostMapping("/copy")
    @Operation(summary = "Copy Directory")
    public ResponseEntity<String> copyDirectory(@RequestBody DirectoryCopyRequest request) {
        try {
            User performedBy = userService.findUserByUuid(request.getUserUuid());

            userPermissionService.checkPermission(performedBy.getUserUuid(), Roles.COPY_DIRECTORY);
            directoryCopyService.copyDirectory(request.getSourceDirGuid(), request.getTargetDirGuid(), performedBy);
            logger.info("Directory copied successfully.");
            return ResponseEntity.ok("Directory copied successfully.");
        } catch (IOException e) {
            logger.error("Directory copy failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error copying directory: " + e.getMessage());
        }
    }
}
