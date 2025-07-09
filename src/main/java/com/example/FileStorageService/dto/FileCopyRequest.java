package com.example.FileStorageService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FileCopyRequest {

    private UUID fileGuid;
    private UUID targetDirectoryGuid;
}
