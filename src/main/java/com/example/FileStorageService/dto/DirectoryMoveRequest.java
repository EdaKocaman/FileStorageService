package com.example.FileStorageService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class DirectoryMoveRequest {
    private final Long directoryID;
    private final Long targetDirectoryID;
    private final UUID userUuid;
}
