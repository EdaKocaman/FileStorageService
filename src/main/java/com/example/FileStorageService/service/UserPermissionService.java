package com.example.FileStorageService.service;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Role;
import com.example.FileStorageService.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPermissionService {
    private static final Logger logger = LoggerFactory.getLogger(UserPermissionService.class);

    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;

    public void checkPermission(UUID userUuid, Roles roles) {
        Role role = roleService.getRoleByEnum(roles)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roles));

        boolean hasPermission = userRoleRepository.existsByUser_UserUuidAndRole(userUuid, role);
        logger.info("Permission check: " + hasPermission);

        if (!hasPermission) {
            logger.error("Permission denied: " + roles);
            throw new RuntimeException("Permission denied: " + roles);
        }
    }

    public boolean hasPermission(UUID userUuid, Roles roles) {
        Role role = roleService.getRoleByEnum(roles)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roles));

        boolean permissionExists = userRoleRepository.findByUser_UserUuidAndRole(userUuid, role).isPresent();
        logger.info("Has permission: " + permissionExists);

        return permissionExists;
    }
}

