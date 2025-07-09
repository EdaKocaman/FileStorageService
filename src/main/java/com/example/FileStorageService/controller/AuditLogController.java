package com.example.FileStorageService.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.service.AuditLogService;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Log", description = "Get audit logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogService.getAllLogs();
    }
}