package com.example.FileStorageService.controller;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.service.UserPermissionService;
import com.example.FileStorageService.service.UserRoleService;
import com.example.FileStorageService.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-roles")
@Tag(name = "User Operations", description = "APIs for user management")
public class UserRoleController {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    private final UserRoleService userRoleService;
    private final UserPermissionService permissionService;
    private final UserService userService;

    @PostMapping("/assign")
    public String assignRole(@RequestParam UUID userUuid, @RequestParam String roleName) {
        permissionService.checkPermission(userUuid, Roles.ASSIGN_ROLE);
        String username = userRoleService.getUsernameByUuid(userUuid);
        userRoleService.assignRoleToUser(username, roleName);
        logger.info("Role assigned successfully!");
        return "Role assigned successfully!";
    }
}
