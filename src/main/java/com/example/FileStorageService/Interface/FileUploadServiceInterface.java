package com.example.FileStorageService.Interface;

import com.example.FileStorageService.dto.FileDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.FileStorageService.model.StoredFile;

import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface FileUploadServiceInterface {
    @PostMapping("/upload")
    StoredFile store(@RequestPart("file") MultipartFile file, Long directoryId, String directoryName, String directoryPath);


    void init();
    Stream<Path> loadAll();
    Path load(String filename);
    Resource loadAsResource(String filename);
    void deleteAll();
    StoredFile getFile(Long id);
    List<FileDTO> getAllFiles();
    void deleteFileID(Long id);

}