package com.example.FileStorageService.dto;

import com.example.FileStorageService.model.Directory;

import java.util.List;
import java.util.stream.Collectors;

public class DirectoryDTO {
    private Long id;
    private String name;
    private String path;

    public DirectoryDTO(Directory directory) {
        this.id = directory.getId();
        this.name = directory.getName();
        this.path = directory.getPath();
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}
}
