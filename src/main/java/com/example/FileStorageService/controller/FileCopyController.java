package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.FileCopyRequest;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.FileCopyService;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Operations")
@RequiredArgsConstructor
public class FileCopyController {
    private static final Logger logger = LoggerFactory.getLogger(FileCopyController.class);
    private final FileCopyService fileCopyService;
    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @PostMapping("/copy")
    @Operation(summary = "Copy File")
    public ResponseEntity<String> copyFile(@RequestBody FileCopyRequest request, @RequestParam("userUuid") UUID userUuid) {
        logger.info("Received request: fileGuid=" + request.getFileGuid() + ", targetDirectoryGuid=" + request.getTargetDirectoryGuid());
        try {
            User currentUser = userService.getUserByUuid(userUuid);
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.COPY_FILE);

            if (request.getFileGuid() == null || request.getTargetDirectoryGuid() == null) {
                logger.error("Error: FileGuid or TargetDirectoryGuid is null");
                return ResponseEntity.badRequest().body("Error: FileGuid or TargetDirectoryGuid is null");
            }

            fileCopyService.copy(request.getFileGuid(), request.getTargetDirectoryGuid());
            logger.info("File copied successfully!");

            return ResponseEntity.ok("File copied successfully!");
        } catch (IOException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
