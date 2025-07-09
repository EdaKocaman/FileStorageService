package com.example.FileStorageService.controller;


import com.example.FileStorageService.dto.DirectoryDeleteRequest;
import com.example.FileStorageService.dto.DirectoryDeleteResponse;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.UserRepository;
import com.example.FileStorageService.service.DirectoryDeleteService;
import com.example.FileStorageService.service.UserPermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directory")
@Tag(name = "Directory Operations", description = "APIs for directory management")
public class DirectoryDeleteController {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryDeleteController.class);

    private final DirectoryDeleteService directoryDeleteService;
    private final UserRepository userRepository;
    private final UserPermissionService userPermissionService;


    @DeleteMapping("/delete")
    public ResponseEntity<DirectoryDeleteResponse> deleteDirectory(@RequestBody DirectoryDeleteRequest request) {
        User performedBy = userRepository.findByUserUuid(request.getUserUuid())
                .orElse(null);

        if (performedBy == null) {
            logger.error("User not found with UUID: {}", request.getUserUuid());
            return ResponseEntity.badRequest().body(new DirectoryDeleteResponse("User not found", false));
        }

        try {
            userPermissionService.checkPermission(performedBy.getUserUuid(), Roles.DELETE_DIRECTORY);
        } catch (RuntimeException e) {
            logger.error("Permission error: {}", e.getMessage());
            return ResponseEntity.status(403).body(new DirectoryDeleteResponse("Permission denied: " + e.getMessage(), false));
        }

        DirectoryDeleteResponse response = directoryDeleteService.deleteDirectory(request.getDirectoryGuid(), performedBy);
        logger.info("Directory {} deleted successfully by user {}", request.getDirectoryGuid(), performedBy.getUserUuid());

        return ResponseEntity.ok(response);
    }
}
