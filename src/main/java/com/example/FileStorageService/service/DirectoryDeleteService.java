package com.example.FileStorageService.service;

import com.example.FileStorageService.dto.DirectoryDeleteResponse;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.AuditLogRepository;
import com.example.FileStorageService.repository.DirectoryRepository;
import com.example.FileStorageService.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DirectoryDeleteService {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryDeleteService.class);
    private final DirectoryRepository directoryRepository;
    private final FileRepository fileRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserPermissionService userPermissionService;

    public DirectoryDeleteResponse deleteDirectory(UUID directoryGuid, User performedBy) {
        try {
            userPermissionService.checkPermission(performedBy.getUserUuid(), Roles.DELETE_DIRECTORY);
        } catch (RuntimeException e) {
            logger.error("Permission denied for user {}: {}", performedBy.getUserUuid(), e.getMessage());
            return new DirectoryDeleteResponse("Permission denied: " + e.getMessage(), false);
        }

        Directory directory = directoryRepository.findByGuid(directoryGuid)
                .orElseThrow(() -> {
                    logger.error("Directory not found: " + directoryGuid);
                    return new RuntimeException("Directory not found: " + directoryGuid);
                });

        File dirFile = new File(directory.getPath());

        try {
            if (dirFile.exists()) {
                FileUtils.deleteDirectory(dirFile);
                logger.info("Directory deleted from filesystem: {}", dirFile.getAbsolutePath());
            } else {
                logger.warn("Directory does not exist in filesystem: {}", dirFile.getAbsolutePath());
            }

            fileRepository.deleteAll(fileRepository.findByDirectory(directory));

            directoryRepository.delete(directory);

            saveAuditLog("DELETE", "Directory", performedBy, directory.getName(), directory.getId(), directory.getPath());

            return new DirectoryDeleteResponse("Directory deleted successfully", true);
        } catch (IOException e) {
            logger.error("Failed to delete directory: {}", directory.getPath(), e);
            return new DirectoryDeleteResponse("Failed to delete directory: " + directory.getPath(), false);
        }
    }

    private void saveAuditLog(String action, String entityType, User user, String entityName, Long entityId, String directoryPath) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        log.setTimestamp(LocalDateTime.now());
        log.setMessage("Deleted successfully. Path: " + directoryPath);

        auditLogRepository.save(log);
    }
}
