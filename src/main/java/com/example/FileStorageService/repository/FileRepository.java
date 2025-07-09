package com.example.FileStorageService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FileStorageService.model.Directory;
import com.example.FileStorageService.model.StoredFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findByDirectory(Directory directory);
    Optional<StoredFile> findByName(String name);
    Optional<StoredFile> findByGuid(UUID fileGuid);
}
