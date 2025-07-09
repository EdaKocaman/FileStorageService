package com.example.FileStorageService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@Getter
@Setter
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guid", updatable = false, nullable = false, unique = true)
    private UUID guid;

    @Column(nullable = false)
    private String name;

    @Column(name = "content_type")
    private String contentType;

    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id")
    private Directory directory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", referencedColumnName = "user_uuid")
    private User uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    public void generateGUID() {
        if (this.guid == null) {
            this.guid = UUID.randomUUID();
        }
    }

    public StoredFile(String name, String contentType, Long size, Directory directory, User uploadedBy) {
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.directory = directory;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = LocalDateTime.now();
    }

    public String getPath() {
        return directory != null ? directory.getPath() : null;
    }

}