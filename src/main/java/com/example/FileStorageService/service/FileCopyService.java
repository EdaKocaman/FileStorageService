package com.example.FileStorageService.service;

import com.example.FileStorageService.Interface.FileCopyServiceInterface;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.StoredFile;
import com.example.FileStorageService.repository.DirectoryRepository;
import com.example.FileStorageService.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileCopyService implements FileCopyServiceInterface {
    private static final Logger logger= LoggerFactory.getLogger(FileCopyService.class);
    private final RestTemplate restTemplate;
    private final FileRepository fileRepository;
    private final DirectoryRepository directoryRepository;

    @Override
    public void copy(UUID fileGuid, UUID targetDirectoryGuid) throws IOException {
        logger.info("Copy operation started. fileGuid: {}, targetDirectoryGuid: {}", fileGuid, targetDirectoryGuid);

        StoredFile storedFile = fileRepository.findByGuid(fileGuid)
                .orElseThrow(() -> {
                    logger.error("Source file not found: {}", fileGuid);
                    return new IOException("Source file not found: " + fileGuid);
                });

        Directory targetDirectory = directoryRepository.findByGuid(targetDirectoryGuid)
                .orElseThrow(() -> {
                    logger.error("Target directory not found: {}", targetDirectoryGuid);
                    return new IOException("Target directory not found: " + targetDirectoryGuid);
                });

        Path targetDirPath = Paths.get(targetDirectory.getPath());

        if (!Files.exists(targetDirPath) || !Files.isDirectory(targetDirPath)) {
            logger.error("Target path is not a valid directory: {}", targetDirPath);
            throw new IOException("Target path is not a valid directory: " + targetDirPath);
        }
        logger.info("Target directory found: {}", targetDirPath);

        Path sourceFilePath = Paths.get(storedFile.getPath(), storedFile.getName());

        if (!Files.exists(sourceFilePath) || !Files.isRegularFile(sourceFilePath)) {
            logger.error("Source is not a valid file: {}", sourceFilePath);
            throw new IOException("Source is not a valid file: " + sourceFilePath);
        }

        File sourceFile = new File(storedFile.getPath());
        File targetFile = new File(targetDirectory.getPath(), storedFile.getName());

        if (sourceFile.isDirectory()) {
            FileUtils.copyDirectory(sourceFile, targetFile);
            logger.info("Directory copied successfully from {} to {}", sourceFile.getPath(), targetFile.getPath());
        } else if (sourceFile.isFile()) {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("File copied successfully from {} to {}", sourceFile.getPath(), targetFile.getPath());
        } else {
            logger.error("Invalid source: {}", sourceFile.getPath());
            throw new IOException("Source is neither a file nor a directory: " + sourceFile.getPath());
        }

        StoredFile copiedFile = new StoredFile(
                sourceFile.getName(),
                storedFile.getContentType(),
                storedFile.getSize(),
                targetDirectory,
                storedFile.getUploadedBy()
        );
        copiedFile.setGuid(UUID.randomUUID());

        fileRepository.save(copiedFile);
        logger.info("New file record created: {}", copiedFile.getGuid());
    }

    @Override
    public void downloadFile(String fileUrl, String savePath) throws IOException {
        byte[] fileBytes = restTemplate.getForObject(fileUrl, byte[].class);

        if (fileBytes != null) {
            try (FileOutputStream fos = new FileOutputStream(new File(savePath))) {
                fos.write(fileBytes);
                fos.flush();
            }
            logger.info("File downloaded successfully to: {}", savePath);
        } else {
            logger.error("Failed to download file from: {}", fileUrl);
            throw new IOException("Failed to download file from: " + fileUrl);
        }
    }
}