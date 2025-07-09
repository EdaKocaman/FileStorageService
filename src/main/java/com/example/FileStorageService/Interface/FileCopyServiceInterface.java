package com.example.FileStorageService.Interface;

import java.io.IOException;
import java.util.UUID;

public interface FileCopyServiceInterface {
    void copy(UUID fileGuid, UUID targetDirectoryGuid) throws IOException;
    void downloadFile(String fileUrl, String savePath) throws IOException;
}
