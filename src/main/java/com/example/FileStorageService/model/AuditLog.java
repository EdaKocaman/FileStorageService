package com.example.FileStorageService.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String entityType; // directory, file
    private Long entityId; //directory, file id
    private String entityName;//directory, file name
    private String message;
    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;
    private LocalDateTime performedAt;
    private LocalDateTime timestamp;

}
