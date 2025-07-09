package com.example.FileStorageService.dto;

public class DirectoryDeleteResponse {
    private String message;
    private boolean success;

    public DirectoryDeleteResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
