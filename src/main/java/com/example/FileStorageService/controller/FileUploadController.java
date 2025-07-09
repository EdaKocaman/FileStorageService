package com.example.FileStorageService.controller;

import com.example.FileStorageService.dto.FileUploadResponse;
import com.example.FileStorageService.dto.FileDTO;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.StoredFile;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.service.AuditLogService;
import com.example.FileStorageService.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.FileStorageService.Interface.FileUploadServiceInterface;
import com.example.FileStorageService.service.UserService;
import com.example.FileStorageService.exception.StorageFileNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Operations", description = "APIs for file management")
@RequiredArgsConstructor
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private final FileUploadServiceInterface fileService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final UserPermissionService permissionService;


    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload a file", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "multipart/form-data")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> uploadFile( @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directoryId", required = false) Long directoryId,
            @RequestParam(value = "directoryName", required = false)String directoryName,
            @RequestParam(value = "directoryPath", required = false) String directoryPath, @RequestParam("userUuid") String userUuid) {
        User currentUser=userService.getCurrentUser();
        logger.info("File upload request received: filename={}, directoryId={}, directoryName={}, directoryPath={}, userUuid={}",
                file.getOriginalFilename(), directoryId, directoryName, directoryPath, userUuid);
        try {
            permissionService.checkPermission(currentUser.getUserUuid(), Roles.UPLOAD_FILE);
            StoredFile savedFile = fileService.store(file, directoryId, directoryName, directoryPath);
            logger.info("File uploaded successfully: id={}, name={}", savedFile.getId(), savedFile.getName());
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage(), e);
            auditLogService.saveAuditLog("FILE UPLOAD FAILED", "Error: " + e.getMessage(), "FILE", null,currentUser  );
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file by ID")
    public ResponseEntity<?> getFile(@PathVariable Long id) {
        logger.info("File retrieval request received for ID: {}", id);
        try {
            StoredFile file = fileService.getFile(id);
            logger.info("File retrieved successfully: id={}, name={}", file.getId(), file.getName());
            return ResponseEntity.ok(file);
        } catch (StorageFileNotFoundException e) {
            logger.warn("File not found: id={}", id);
            return ResponseEntity.status(404).body("File not found: " + e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        logger.info("FileUploadController loaded successfully.");
    }

    @GetMapping("/list")
    @Operation(summary = "List files")
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        List<FileDTO> fileDTOs = fileService.getAllFiles();
        return ResponseEntity.ok(fileDTOs);
    }
}