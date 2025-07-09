package com.example.FileStorageService.dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class FileMoveResponse {
    private final UUID fileGuid;
    private final UUID targetDirectoryID;
    private final String status;
}
