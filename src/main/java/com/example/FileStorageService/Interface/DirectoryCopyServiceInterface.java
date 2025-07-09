package com.example.FileStorageService.Interface;
import com.example.FileStorageService.model.User;

import java.io.IOException;
import java.util.UUID;

public interface DirectoryCopyServiceInterface {
    void copyDirectory(UUID sourceDirGuid, UUID targetDirGuid, User performedBy) throws IOException;
}
