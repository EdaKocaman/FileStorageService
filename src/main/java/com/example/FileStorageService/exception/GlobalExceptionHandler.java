package com.example.FileStorageService.exception;

import com.example.FileStorageService.controller.FileMoveController;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.UserRepository;
import com.example.FileStorageService.service.AuditLogService;
import com.example.FileStorageService.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(FileMoveController.class);

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        String errorMessage = e.getMessage();
        User currentUser = getCurrentUser();

        // Log kaydını ekle
        auditLogService.saveAuditLog("ERROR", errorMessage, "ERROR", null, currentUser);

        // JSON formatında bir response oluştur
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.put("message", errorMessage);
        response.put("errorCode", "UNKNOWN_ERROR");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({StorageException.class, StorageFileNotFoundException.class})
    public ResponseEntity<String> handleStorageExceptions(Exception ex) {
        User currentUser= getCurrentUser();
        if (ex instanceof StorageFileNotFoundException) {
            auditLogService.saveAuditLog("ERROR", ex.getMessage(), "FILE_NOT_FOUND", null, currentUser);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found: " + ex.getMessage());
        } else if (ex instanceof StorageException) {
            auditLogService.saveAuditLog("ERROR", ex.getMessage(), "STORAGE_ERROR", null, currentUser);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Storage error occurred: " + ex.getMessage());
        }
        auditLogService.saveAuditLog("ERROR", ex.getMessage(), "UNKNOWN_ERROR", null, currentUser);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(FileNotFoundException ex, WebRequest request) {
        auditLogService.saveAuditLog("ERROR", ex.getMessage(), "FILE_NOT_FOUND", null, getCurrentUser());
        return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
    }
    private User getCurrentUser() {
        return userService.getCurrentUser();
    }
        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User is not authenticated.");
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    private User defaultUser() {
        return userRepository.findByUsername("EdaKocaman"); // Burada tek kullanıcıyı döndür
    }*/
}
