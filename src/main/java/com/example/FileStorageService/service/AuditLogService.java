package com.example.FileStorageService.service;

import com.example.FileStorageService.model.User;
import com.example.FileStorageService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.FileStorageService.model.AuditLog;
import com.example.FileStorageService.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public void saveAuditLog(String action, String message, String entityType, Long entityId, User performedBy) {

        if (performedBy == null) {
            logger.error("Audit log must be performed by a user.");
            throw new RuntimeException("Audit log must be performed by a user.");
        }

        AuditLog auditLog = getAuditLog(action, message, entityType, entityId, performedBy);

        auditLogRepository.save(auditLog);
        logger.info("Audit log saved: Action={}, EntityType={}, PerformedBy={}", action, entityType, performedBy.getUserUuid());
    }

    private static AuditLog getAuditLog(String action, String message, String entityType, Long entityId, User performedBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setMessage(message);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedBy(performedBy);
        return auditLog;
    }

} 