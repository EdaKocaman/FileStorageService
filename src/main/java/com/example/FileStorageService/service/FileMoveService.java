package com.example.FileStorageService.service;

import com.example.FileStorageService.Interface.FileMoveServiceInterface;
import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.AuditLogRepository;
import com.example.FileStorageService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.StoredFile;
import com.example.FileStorageService.repository.DirectoryRepository;
import com.example.FileStorageService.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileMoveService implements FileMoveServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(FileMoveService.class);
    private final FileRepository fileRepository;
    private final DirectoryRepository directoryRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRoleService userRoleService;

    @Override
    public void moveFile(UUID fileGuid, Long targetDirectoryID, UUID userUuid){
        try{
            // File lookup
            Optional<StoredFile> fileOpt = fileRepository.findByGuid(fileGuid);
            if (fileOpt.isEmpty()) {
                logger.error("File not found with GUID: {}", fileGuid);
                throw new RuntimeException("File not found.");
            }

            //Directory lookup
            Optional<Directory> directoryOpt = directoryRepository.findById(targetDirectoryID);
            if (directoryOpt.isEmpty()) {
                logger.error("Target directory not found with ID: {}", targetDirectoryID);
                throw new RuntimeException("Target directory not found.");
            }

            // User lookup by UUID
            Optional<User> userOpt = userRepository.findByUserUuid(userUuid);
            if (userOpt.isEmpty()) {
                logger.error("User not found with UUID: {}", userUuid);
                throw new RuntimeException("User not found.");
            }

            StoredFile recentFile = fileOpt.get();
            Directory targetDirectory = directoryOpt.get();
            User performedBy = userOpt.get();

            /*if (!userRoleService.hasPermission(userUuid, "MOVE_FILE")) {
                logger.error("User {} does not have permission to move files", userUuid);
                throw new RuntimeException("User does not have permission to move files.");
            }*/

            // Check if directories and paths are valid
            if (recentFile.getDirectory() == null || targetDirectory.getPath() == null) {
                logger.error("Invalid directory path.");
                throw new RuntimeException("Invalid directory path.");
            }

            // Moving the file
            File sourceFile = new File(recentFile.getDirectory().getPath(), recentFile.getName());
            File targetDir = new File(targetDirectory.getPath());

            logger.info("Source file path: {}", sourceFile.getAbsolutePath());
            logger.info("Target directory path: {}", targetDir.getAbsolutePath());

            FileUtils.moveToDirectory(sourceFile, targetDir, true);
            logger.info("File {} moved to directory {}", fileGuid, targetDirectoryID);

            // Update file record with new directory information
            recentFile.setDirectory(targetDirectory);
            fileRepository.save(recentFile);


            // Log the action
            saveAuditLog("MOVE", "FILE", performedBy, recentFile.getName(), recentFile);


        }catch (Exception e){
            logger.error("Error occurred while moving file: " + e.getMessage());
            throw new RuntimeException("File move failed: " + e.getMessage());
        }
    }

    private void saveAuditLog(String action, String entityType, User user, String fileName, StoredFile file) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(file.getId());
        log.setEntityName(fileName);
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        log.setTimestamp(LocalDateTime.now());
        log.setMessage("File moved successfully");
        auditLogRepository.save(log);
    }
}
