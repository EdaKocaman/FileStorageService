package com.example.FileStorageService.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryCopyResponse {

    private String message;
    private boolean success;

    public DirectoryCopyResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    @Override
    public String toString() {
        return "DirectoryCopyResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
