package com.example.FileStorageService.service;

import com.example.FileStorageService.dto.DirectoryDTO;
import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.exception.UserPermissionException;
import com.example.FileStorageService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.apache.commons.lang3.Validate;

import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.AuditLogRepository;
import com.example.FileStorageService.repository.DirectoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectoryCreateService {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryCreateService.class);
    private final DirectoryRepository directoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final UserPermissionService userPermissionService;

    @Transactional
    public Directory createDirectory(String name, String path, Long parentId, User user) {
        try {
            Validate.notBlank(name, "Directory name cannot be empty");
            Validate.notNull(user, "User cannot be null");

            userPermissionService.checkPermission(user.getUserUuid(), Roles.CREATE_DIRECTORY);

            //Burası değişecek base path böyle olmamalı
            String basePath = "/Users/edakocaman/Downloads/FileStorageService/upload-dir";
            String fullPath= basePath;
            Directory parent = null;

            if (StringUtils.isNotBlank(path)) {
                path = path.replace("\"", "").trim();
                fullPath = Paths.get(path, name).toString();
            } else if (parentId != null) {
                parent = directoryRepository.findById(parentId)
                        .orElseThrow(() -> {
                            logger.error("Parent directory not found for parentId: {}", parentId);
                            return new IllegalArgumentException("Parent directory not found");
                        });
                fullPath = parent.getPath() + "/" + name;
            } else {
                fullPath = basePath + "/" + name;
                logger.info("Full Path: "+fullPath);
            }

            Path directoryPath = Paths.get(fullPath);
            if (Files.exists(directoryPath)) {
                logger.error("Directory already exists: " + fullPath);
                throw new IllegalStateException("Directory already exists: " + fullPath);
            }
            Files.createDirectories(directoryPath);

            Directory directory = getDirectory(name, user, fullPath, parent);
            logAudit(directory,"CREATE", "DIRECTORY", user);
            return directoryRepository.save(directory);
        } catch (SecurityException e) {
            logger.error("Permission denied: " + e.getMessage());
            throw new UserPermissionException("User does not have permission to create directories");
        } catch (IOException e) {
            logger.error("Could not create directory: " + e.getMessage());
            throw new RuntimeException("Could not create directory: " + e.getMessage());
        }
    }

    private static Directory getDirectory(String name, User user, String fullPath, Directory parent) {
        Validate.notBlank(name, "Directory name cannot be empty");
        Validate.notNull(user, "User cannot be null");
        Validate.notBlank(fullPath, "Directory path cannot be empty");

        Directory directory = new Directory();
        directory.setName(name);
        directory.setPath(fullPath);
        directory.setCreatedBy(user);
        directory.setParent(parent);
        directory.setCreatedAt(LocalDateTime.now());
        directory.setUpdatedBy(user);
        directory.setUpdatedAt(LocalDateTime.now());
        logger.info("GET DIRECTORY: " + "name: "+ name + "full path: " + fullPath +
                "user: " + user + "parent: " + parent);
        return directory;
    }

    private void logAudit(Directory directory,String action, String entityType,User user) {
        Validate.notNull(directory, "Directory cannot be null");
        Validate.notBlank(action, "Action cannot be empty");
        Validate.notBlank(entityType, "EntityType cannot be empty");
        Validate.notNull(user, "User cannot be null");

        User existingUser = userRepository.findByUserUuid(user.getUserUuid())
                .orElseThrow(() -> {
                    logger.error("User not found");
                    return new RuntimeException("User not found");
                });


        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(directory.getId());
        log.setEntityName(getFullPath(directory));
        log.setPerformedBy(existingUser);
        log.setPerformedAt(LocalDateTime.now());
        log.setTimestamp(LocalDateTime.now());
        log.setMessage("Directory created successfully");
        logger.info("Directory created successfully (logAudit)");
        auditLogRepository.save(log);
    }

    private String getFullPath(Directory directory) {
        Validate.notNull(directory, "Directory cannot be null");
        Directory current = directory;
        StringBuilder path = new StringBuilder(directory.getName());
        while (current.getParent() != null) {
            current = current.getParent();
            path.insert(0, current.getName() + "/");
        }
        return path.toString();
    }

    public List<DirectoryDTO> getAllDirectories() {
        List<Directory> directories = directoryRepository.findAll();
        Validate.notEmpty(directories, "No directories found in the database");
        return searchDirectories("");
    }

    public DirectoryDTO getDirectoryTree(Long directoryId) {
        return new DirectoryDTO(getDirectory(directoryId));
    }

    public Directory getDirectory(Long id) {
        Validate.notNull(id, "Directory ID cannot be null");
        return restClient.get()
                .uri("/api/directories/{id}", id)
                .retrieve()
                .body(Directory.class);
    }

    public Directory createDirectoryExternal(Directory directory) {
        Validate.notNull(directory, "Directory cannot be null");
        return restClient.post()
                .uri("/api/directories")
                .body(directory)
                .retrieve()
                .body(Directory.class);
    }

    public Directory updateDirectory(Long id, Directory directory) {
        Validate.notNull(id, "Directory ID cannot be null");
        Validate.notNull(directory, "Directory cannot be null");
        return restClient.put()
                .uri("/api/directories/{id}", id)
                .body(directory)
                .retrieve()
                .body(Directory.class);
    }

    public void deleteDirectory(Long id) {
        Validate.notNull(id, "Directory ID cannot be null");
        restClient.delete()
                .uri("/api/directories/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public List<DirectoryDTO> searchDirectories(String name) {
        Validate.notBlank(name, "Search name cannot be empty");
        List<Directory> directories = restClient.get().
                uri(uriBuilder -> uriBuilder
                        .path("/api/directories/search")
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Directory>>() {});

        return directories.stream()
                .map(DirectoryDTO::new)
                .collect(Collectors.toList());
    }
}