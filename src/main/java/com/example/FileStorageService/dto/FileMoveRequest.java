package com.example.FileStorageService.dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@RequiredArgsConstructor
@Getter
@Setter
public class FileMoveRequest {
    private final UUID fileGuid;
    private final Long targetDirectoryID;
    private final UUID userUuid;
}
