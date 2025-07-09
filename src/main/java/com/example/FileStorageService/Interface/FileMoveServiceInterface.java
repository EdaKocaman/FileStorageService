package com.example.FileStorageService.Interface;

import java.io.IOException;
import java.util.UUID;

public interface FileMoveServiceInterface {
    void moveFile(UUID fileGuid, Long targetDirectoryID, UUID userUuid) throws IOException;
}
