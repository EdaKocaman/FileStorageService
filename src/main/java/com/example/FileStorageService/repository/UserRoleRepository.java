package com.example.FileStorageService.repository;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Role;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findAllByUser_UserUuid(UUID userUuid);

    Optional<UserRole> findByUser_UserUuid(UUID userUuid);

    Optional<UserRole> findByUser_UserUuidAndRole(UUID userUuid, Role role);

    boolean existsByUser_UserUuidAndRole(UUID userUuid, Role role);

}