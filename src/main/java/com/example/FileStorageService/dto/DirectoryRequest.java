package com.example.FileStorageService.dto;

import java.util.UUID;

public class DirectoryRequest {
    private String fullPath;
    private String name;
    private String path;
    private Long parentId;
    
    public Long getParentId() {return parentId;}
    public void setParentId(Long parentId) {this.parentId = parentId;}
    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getFullPath() {return fullPath;}
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    public UUID getUserId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
    }
} 