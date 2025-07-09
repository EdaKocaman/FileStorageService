package com.example.FileStorageService.service;


import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Role;
import com.example.FileStorageService.model.User;
import com.example.FileStorageService.model.UserRole;
import com.example.FileStorageService.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleService.class);
    private final UserRoleRepository userRoleRepository;
    private final UserService userService;
    private final RoleService roleService;

    public List<UserRole> getUserRoles(User user) {
        return userRoleRepository.findAllByUser_UserUuid(user.getUserUuid());
    }

    public UserRole saveUserRole(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    public String getUsernameByUuid(UUID userUuid) {
        User user = userService.getUserByUuid(userUuid);
        if (user == null) {
            logger.info("User not found with UUID: {}", userUuid);
            throw new RuntimeException("User not found with UUID: " + userUuid);
        }
        return user.getUsername();
    }

    public void assignRoleToUser(String username, String roleName) {
        logger.info("Assigning role '{}' to user '{}'", roleName, username);
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found");
                    return new RuntimeException("User not found");
                });

        Roles requestedRole = Roles.valueOf(roleName);

        Role role = roleService.getRoleByEnum(requestedRole)
                .orElseThrow(() -> {
                    logger.error("Role '{}' not found", roleName);
                    return new RuntimeException("Role not found");
                });

        boolean roleExists = userRoleRepository.existsByUser_UserUuidAndRole(user.getUserUuid(), role);

        if (roleExists) {
            logger.warn("Role '{}' is already assigned to user '{}'", roleName, username);
            throw new RuntimeException("Role already assigned to this user");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        try {
            userRoleRepository.save(userRole);
            logger.info("Role '{}' successfully assigned to user '{}'", roleName, username);
        } catch (Exception e) {
            logger.error("Error assigning role '{}' to user '{}'", roleName, username, e);
            throw new RuntimeException("Error assigning role", e);
        }
    }
}
