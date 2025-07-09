package com.example.FileStorageService.dto;

import com.example.FileStorageService.model.StoredFile;

import java.time.LocalDateTime;

public class FileDTO {
    private Long id;
    private String name;
    private Long size;
    private String contentType;
    private LocalDateTime uploadedAt;
    private DirectoryDTO directory;

    public FileDTO(StoredFile file) {
        this.id = file.getId();
        this.name = file.getName();
        this.size = file.getSize();
        this.contentType = file.getContentType();
        this.uploadedAt = file.getUploadedAt();
        if (file.getDirectory() != null) {
            this.directory = new DirectoryDTO(file.getDirectory());
        } else {
            this.directory = null;
        }
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public DirectoryDTO getDirectory() {
        return directory;
    }
    public void setDirectory(DirectoryDTO directory) {
        this.directory = directory;
    }
}
