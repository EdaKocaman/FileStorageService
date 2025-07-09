package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.DirectoryMoveRequest;
import com.example.FileStorageService.dto.DirectoryMoveResponse;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.DirectoryMoveService;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/directories")
@Tag(name = "Directory Operations", description = "APIs for directory management")
@RequiredArgsConstructor
public class DirectoryMoveController {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryMoveController.class);
    private final DirectoryMoveService directoryMoveService;
    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @PostMapping("/move/{directoryID}/{targetDirectoryID}")
    @Operation(summary = "Move directory")
    public ResponseEntity<DirectoryMoveResponse> moveDirectory(@RequestBody DirectoryMoveRequest request) {
        logger.info("Received request to move directory {} to {}", request.getDirectoryID(), request.getTargetDirectoryID());

        try {
            User currentUser = userService.getUserByUuid(request.getUserUuid());
            userPermissionService.checkPermission(currentUser.getUserUuid(), Roles.MOVE_DIRECTORY);

            directoryMoveService.moveDirectory(request.getDirectoryID(), request.getTargetDirectoryID(), currentUser);
            DirectoryMoveResponse response = new DirectoryMoveResponse(
                    request.getDirectoryID(),
                    request.getTargetDirectoryID(),
                    request.getUserUuid(),
                    "Directory moved successfully."
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error moving directory: {}", e.getMessage());

            DirectoryMoveResponse errorResponse = new DirectoryMoveResponse(
                    request.getDirectoryID(),
                    request.getTargetDirectoryID(),
                    request.getUserUuid(),
                    "Error: " + e.getMessage()
            );

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
