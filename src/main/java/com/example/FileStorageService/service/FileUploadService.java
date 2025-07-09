package com.example.FileStorageService.service;

import com.example.FileStorageService.Interface.FileUploadServiceInterface;
import com.example.FileStorageService.dto.FileDTO;
import com.example.FileStorageService.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;


import com.example.FileStorageService.StorageProperties;
import com.example.FileStorageService.exception.StorageException;
import com.example.FileStorageService.exception.StorageFileNotFoundException;
import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.StoredFile;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.AuditLogRepository;
import com.example.FileStorageService.repository.DirectoryRepository;
import com.example.FileStorageService.repository.FileRepository;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;


@Service
public class FileUploadService implements FileUploadServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    private final Path rootLocation;
    @Autowired
    private final FileRepository fileRepository;
    private final UserService userService;
    private final AuditLogRepository auditLogRepository;
    private final DirectoryRepository directoryRepository;
    @Autowired
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Autowired
    public FileUploadService(StorageProperties properties,
                             FileRepository fileRepository,
                             UserService userService,
                             AuditLogRepository auditLogRepository,
                             DirectoryRepository directoryRepository, AuditLogService auditLogService, UserRepository userRepository) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.fileRepository = fileRepository;
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
        this.directoryRepository = directoryRepository;
        this.auditLogService= auditLogService;
        this.userRepository=userRepository;
    }

    @Override
    @PostMapping("/upload")
    public StoredFile store(@RequestPart("file") MultipartFile file, Long directoryId,
                            String directoryName, String directoryPath) {
        User currentUser = userService.getCurrentUser();
        try {
            if (file.isEmpty()) {
                auditLogService.saveAuditLog("FILE UPLOAD FAILED", "Failed to store empty file.", "FILE", null, currentUser);
                logger.error("Failed to store empty file.");
                throw new StorageException("Failed to store empty file.");
            }

            String originalFilename = file.getOriginalFilename();
            String fileName = StringUtils.cleanPath(originalFilename);

            if (originalFilename == null) {
                auditLogService.saveAuditLog("FILE UPLOAD FAILED", "Failed to store file with null filename", "FILE", null, currentUser);
                logger.error("Failed to store file with null filename");
                throw new StorageException("Failed to store file with null filename");
            }

            if (fileName.contains("..")) {
                auditLogService.saveAuditLog("FILE UPLOAD FAILED", "Cannot store file with relative path outside current directory " + fileName, "FILE", null, currentUser);
                logger.error("Cannot store file with relative path outside current directory ");
                throw new StorageException("Cannot store file with relative path outside current directory " + fileName);
            }

            Directory directory= findDirectory(directoryId, directoryName, directoryPath);
            if (directoryPath == null || directoryPath.isEmpty()) {
                directoryPath = directory.getPath();
            }
            logger.info("Directory Path: " + directoryPath);

            Path targetDirectory = rootLocation.resolve(directoryPath);
            Files.createDirectories(targetDirectory);
            logger.info("Target Directory: " + targetDirectory);

            File destinationFile = new File(targetDirectory.toFile(), fileName);
            try (InputStream inputStream = file.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, destinationFile);
            }

            String contentType = file.getContentType();
            StoredFile storedFile = new StoredFile(fileName, contentType, file.getSize(), directory, currentUser);
            storedFile = fileRepository.save(storedFile);
            logger.info("File saved successfully: " + fileName);
            //auditLogService.saveAuditLog("File uploaded successfully", "File: " + file.getOriginalFilename());


            auditLog("UPLOAD", "FILE", currentUser, fileName, storedFile);
            logger.info("Audit log saved");
            //auditLogService.saveAuditLog("Audit log saved", "UPLOAD FILE " + file.getOriginalFilename());

            return storedFile;

        } catch (IOException e) {
            auditLogService.saveAuditLog("FILE UPLOAD FAILED", "Error: " + e.getMessage(), "FILE", null, currentUser);
            logger.error("FILE UPLOAD FAILED: "+ e.getMessage());
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
    private Directory findDirectory(Long directoryId, String directoryName, String directoryPath) {
        if (directoryId != null) {
            logger.error("Directory not found with ID: " + directoryId);
            return directoryRepository.findById(directoryId)
                    .orElseThrow(() -> new RuntimeException("Directory not found with ID: " + directoryId));

        } else if (directoryName != null) {
            List<Directory> directories = directoryRepository.findByName(directoryName);
            if (directories.isEmpty()) {
                logger.error("Directory not found with name: " + directoryName);
                throw new RuntimeException("Directory not found with name: " + directoryName);
            } else if (directories.size() > 1) {
                logger.error("Multiple directories found with name: " + directoryName);
                throw new RuntimeException("Multiple directories found with name: " + directoryName);
            }
            return directories.get(0);

        } else if (directoryPath != null) {
            logger.info("Attempting to find directory with path: {}", directoryPath);
            return directoryRepository.findByPath(directoryPath).
                    orElseThrow(() ->{
                        logger.error("Directory not found with path: {}", directoryPath);
                        return new RuntimeException("Directory not found with path: " + directoryPath);
                    });

        }else {
            logger.error("Default directory '/root' not found.");
            return directoryRepository.findByName("mainParent1")
                    .stream().findFirst()
                    .orElseThrow(() -> new StorageException("Default directory '/root' not found."));
        }
    }
    @Transactional
    private void auditLog(String action, String entityType, User user, String fileName, StoredFile file) {
        if (user != null && user.getUserId() != null) {
            User existingUser = userRepository.findByUserUuid(user.getUserUuid())
                    .orElseThrow(() -> {
                        logger.error("User not found with id: " + user.getUserUuid());
                        return new RuntimeException("User not found with id: " + user.getUserUuid());
                    });

            saveAuditLog(action, entityType, existingUser, fileName, file);
        } else {
            logger.error("User is null or has no valid ID. Audit log not saved.");
            throw new RuntimeException("User is null or has no valid ID. Cannot save audit log.");
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
        log.setMessage("File saved successfully");
        auditLogRepository.save(log);
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            logger.error("Could not initialize storage: " + e);
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            logger.error("Failed to read stored files: " + e);
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public List<FileDTO> getAllFiles() {
        List<StoredFile> files = fileRepository.findAll();
        logger.info("getAllFiles");
        return files.stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public StoredFile getFile(Long id) {
        logger.error("File not found with id: " + id);
        return fileRepository.findById(id)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found with id: " + id));
    }

    @Override
    public Path load(String fileName) {
        return rootLocation.resolve(fileName);
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = load(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void deleteFileID(Long fileId) {
        StoredFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> {
                    logger.error("File not found with ID: " + fileId);
                    return new RuntimeException("File not found with ID: " + fileId);
                });
        try {
            Path filePath = rootLocation.resolve(file.getName());
            Files.deleteIfExists(filePath);

            fileRepository.delete(file);

            User currentUser = userService.getCurrentUser();
            auditLog("DELETE", "file", currentUser, file.getName(), file);
        } catch (IOException e) {
            throw new StorageException("Could not delete file: " + file.getName(), e);
        }
    }
}
