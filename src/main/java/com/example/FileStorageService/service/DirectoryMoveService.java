package com.example.FileStorageService.service;

import com.example.FileStorageService.Interface.DirectoryMoveServiceInterface;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.DirectoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Optional;

@Controller
@RequiredArgsConstructor

public class DirectoryMoveService implements DirectoryMoveServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryMoveService.class);
    private final DirectoryRepository directoryRepository;
    private final UserService userService;
    private final UserPermissionService userPermissionService;
    @Transactional
    public void moveDirectory(Long directoryID, Long targetDirectoryID, User performedBy) {
        try {
            userPermissionService.checkPermission(performedBy.getUserUuid(), Roles.MOVE_DIRECTORY);

            Optional<Directory> sourceDirectoryOpt = directoryRepository.findById(directoryID);
            if (sourceDirectoryOpt.isEmpty()) {
                logger.error("Source directory not found with ID: {}", directoryID);
                throw new RuntimeException("Source directory not found.");
            }

            Optional<Directory> targetDirectoryOpt = directoryRepository.findById(targetDirectoryID);
            if (targetDirectoryOpt.isEmpty()) {
                logger.error("Target directory not found with ID: {}", targetDirectoryID);
                throw new RuntimeException("Target directory not found.");
            }

            Directory sourceDirectory = sourceDirectoryOpt.get();
            Directory targetDirectory = targetDirectoryOpt.get();
            logger.info("Source Directory: {}", sourceDirectory);
            logger.info("Target Directory: {}", targetDirectory);


            if (targetDirectory.equals(sourceDirectory) || isSubdirectoryOf(targetDirectory, sourceDirectory)) {
                logger.error("Cannot move directory: Target directory is the same as the source directory or is a subdirectory of the source.");
                throw new RuntimeException("Cannot move directory: Invalid target directory.");
            }

            if (sourceDirectory.getPath() == null || targetDirectory.getPath() == null) {
                logger.error("Invalid directory path.");
                throw new RuntimeException("Invalid directory path.");
            }

            File sourceDir = new File(sourceDirectory.getPath());
            File targetDir = new File(targetDirectory.getPath());
            logger.info("Source Directory: {}", sourceDir);
            logger.info("Target Directory: {}", targetDir);

            logger.info("Source directory path: {}", sourceDir.getAbsolutePath());
            logger.info("Target directory path: {}", targetDir.getAbsolutePath());

            FileUtils.moveDirectoryToDirectory(sourceDir, targetDir, true);
            logger.info("Directory {} moved to directory {}", sourceDirectory.getName(), targetDirectory.getName());

            sourceDirectory.setParent(targetDirectory);

            String newPath = targetDirectory.getPath() + File.separator + sourceDirectory.getName();
            sourceDirectory.setPath(newPath);

            directoryRepository.save(sourceDirectory);
            directoryRepository.flush();

        } catch (Exception e) {
            logger.error("Error occurred while moving directory: {}", e.getMessage());
            throw new RuntimeException("Directory move failed: " + e.getMessage());
        }
    }
    private boolean isSubdirectoryOf(Directory potentialParent, Directory directoryToCheck) {
        Directory parent = directoryToCheck.getParent();
        while (parent != null) {
            if (parent.equals(potentialParent)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }


}
