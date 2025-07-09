package com.example.FileStorageService.dto;

import java.util.UUID;

public class DirectoryCopyRequest {

    private UUID sourceDirGuid;
    private UUID targetDirGuid;
    private UUID userUuid;

    public UUID getSourceDirGuid() {
        return sourceDirGuid;
    }

    public void setSourceDirGuid(UUID sourceDirGuid) {
        this.sourceDirGuid = sourceDirGuid;
    }

    public UUID getTargetDirGuid() {
        return targetDirGuid;
    }

    public void setTargetDirGuid(UUID targetDirGuid) {
        this.targetDirGuid = targetDirGuid;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(UUID userUuid) {
        this.userUuid = userUuid;
    }

    // ToString (isteğe bağlı)
    @Override
    public String toString() {
        return "DirectoryCopyRequest{" +
                "sourceDirGuid=" + sourceDirGuid +
                ", targetDirGuid=" + targetDirGuid +
                ", userUuid=" + userUuid +
                '}';
    }
}
