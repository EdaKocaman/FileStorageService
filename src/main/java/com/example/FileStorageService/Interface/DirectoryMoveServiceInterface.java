package com.example.FileStorageService.Interface;
import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.User;

import java.util.UUID;

public interface DirectoryMoveServiceInterface {
    void moveDirectory(Long directoryID, Long targetDirectoryID, User performedBy);
}
