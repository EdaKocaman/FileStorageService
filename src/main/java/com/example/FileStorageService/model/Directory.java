package com.example.FileStorageService.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "directories")
@Data
@Getter
@Setter
@RequiredArgsConstructor
public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guid", updatable = false, nullable = false, unique = true)
    private UUID guid;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "directory")
    private List<StoredFile> files;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Directory parent;

    @OneToMany(mappedBy = "parent")
    private List<Directory> subdirectories;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
   
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    private LocalDateTime updatedAt;

    @Column(name = "path")
    private String path;

    @Override
    public String toString() {
        return "Directory{id=" + id +
                ", guid=" + guid +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", parent=" + (parent != null ? parent.getName() : "null") +
                ", createdBy=" + (createdBy != null ? createdBy.getUsername() : "null") +
                ", createdAt=" + createdAt +
                ", updatedBy=" + (updatedBy != null ? updatedBy.getUsername() : "null") +
                ", updatedAt=" + updatedAt + '}';
    }


    @PrePersist
    public void generateGUID() {
        if (this.guid == null) {
            this.guid = UUID.randomUUID();
        }
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {updatedAt = LocalDateTime.now();}
}
