package com.example.FileStorageService.repository;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(Roles roleEnum);
    //Optional<Role> findByRoleName(String roleName);


}
