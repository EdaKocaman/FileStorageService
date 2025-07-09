package com.example.FileStorageService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DirectoryDeleteRequest {
    private UUID directoryGuid;
    private UUID userUuid;

}
