package com.example.FileStorageService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FileCopyResponse{
    private UUID originalFileGuid;
    private UUID copiedFileGuid;
    private UUID targetDirectoryGuid;
}
