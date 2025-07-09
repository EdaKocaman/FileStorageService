package com.example.FileStorageService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FileStorageService.model.Directory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    Optional<Directory> findByNameAndParent(String name, Directory parent);
    Optional<Directory> findByPath(String path);
    List<Directory> findByName(String name);
    Optional<Directory> findByGuid(UUID guid);
    boolean existsByGuid(UUID guid);
}