package com.example.FileStorageService.service;

import com.example.FileStorageService.Interface.DirectoryCopyServiceInterface;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.StoredFile;
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
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DirectoryCopyService implements DirectoryCopyServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryCopyService.class);
    private final DirectoryRepository directoryRepository;
    private final FileRepository fileRepository;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;
    private final UserPermissionService userPermissionService;

    @Override
    public void copyDirectory(UUID sourceDirGuid, UUID targetDirGuid, User performedBy) throws IOException {
        userPermissionService.checkPermission(performedBy.getUserUuid(), Roles.COPY_DIRECTORY);

        Directory sourceDir = directoryRepository.findByGuid(sourceDirGuid)
                .orElseThrow(() -> {
                    logger.error("Source directory not found: " + sourceDirGuid);
                    return new IOException("Source directory not found: " + sourceDirGuid);
                });

        Directory targetDir = directoryRepository.findByGuid(targetDirGuid)
                .orElseThrow(() -> {
                    logger.error("Target directory not found: " + targetDirGuid);
                    return new IOException("Target directory not found: " + targetDirGuid);
                });

        Path targetDirPath = Paths.get(targetDir.getPath());

        if (!Files.exists(targetDirPath) || !Files.isDirectory(targetDirPath)) {
            logger.error("Target path is not a valid directory: {}", targetDirPath);
            throw new IOException("Target path is not a valid directory: " + targetDirPath);
        }
        logger.info("Target directory found: {}", targetDirPath);

        File sourcePath = new File(sourceDir.getPath());
        File targetPath = new File(targetDir.getPath(), sourceDir.getName() + "_copy");

        logger.info("SourcePath: " + sourcePath);
        logger.info("TargetPath: " + targetPath);

        if (!sourcePath.exists()) {
            logger.error("Source directory does not exist: " + sourcePath);
            throw new IOException("Source directory does not exist: " + sourcePath);
        }

        FileUtils.copyDirectory(sourcePath, targetPath);
        logger.info("Directory copied successfully: {} -> {}", sourcePath, targetPath);

        Directory copiedDir = copiedDirectory(sourceDir, targetPath, performedBy, targetDir);

        if (directoryRepository.existsByGuid(copiedDir.getGuid())) {
            logger.info("Directory already exists, skipping save.");
        } else {
            directoryRepository.save(copiedDir);
            logger.info("New directory record created: {}", copiedDir.getGuid());
        }
        saveAuditLog("COPY", "Directory", performedBy, copiedDir.getName(),
                copiedDir.getId(), sourceDir.getPath(), targetDir.getPath());

        Directory copiedD = getDirectory(sourceDir, targetDir, targetPath);

        saveFiles(sourceDir, copiedD);
    }

    private static Directory copiedDirectory(Directory sourceDir, File targetPath,
                                             User performedBy, Directory targetDir) {
        Directory copiedDirectory = new Directory();
        copiedDirectory.setName(sourceDir.getName() + "_copy");
        copiedDirectory.setPath(targetPath.getAbsolutePath());
        copiedDirectory.setCreatedBy(performedBy);
        copiedDirectory.setUpdatedBy(performedBy);
        copiedDirectory.setGuid(UUID.randomUUID());
        copiedDirectory.setCreatedAt(LocalDateTime.now());
        copiedDirectory.setUpdatedAt(LocalDateTime.now());
        copiedDirectory.setParent(targetDir);
        return copiedDirectory;
    }
    private void saveAuditLog(String action, String entityType, User user, String entityName, Long entityId,
                              String sourcePath, String targetPath) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        log.setTimestamp(LocalDateTime.now());

        log.setMessage("Copied successfully. " +
                "Source: " + sourcePath +
                " → Target: " + targetPath);

        auditLogRepository.save(log);
    }

    private void saveFiles(Directory sourceDir, Directory copiedDir) {
        for (StoredFile file : fileRepository.findByDirectory(sourceDir)) {
            StoredFile copiedFile = new StoredFile();
            copiedFile.setGuid(UUID.randomUUID());
            copiedFile.setName(file.getName() + "_copy");
            copiedFile.setDirectory(copiedDir);
            copiedFile.setUploadedAt(LocalDateTime.now());
            fileRepository.save(copiedFile);
        }
    }

    private Directory getDirectory(Directory sourceDir, Directory targetDir, File targetPath) {
        Directory copiedDir = new Directory();
        copiedDir.setGuid(UUID.randomUUID());
        copiedDir.setName(sourceDir.getName() + "_copy");
        copiedDir.setPath(targetPath.getAbsolutePath());
        copiedDir.setParent(targetDir);
        directoryRepository.save(copiedDir);
        return copiedDir;
    }
}
