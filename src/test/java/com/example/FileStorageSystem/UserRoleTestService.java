package com.example.FileStorageSystem;


import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.UserRole;
import com.example.FileStorageService.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserRoleTestService {
    @Autowired
    private UserRoleRepository userRoleRepository;

    public void testMethods() {
        UUID userUuid = UUID.randomUUID();
        Long roleId = 1L;
        boolean exists = userRoleRepository.existsByUser_UserUuidAndRoleId(userUuid, roleId);
        System.out.println("Existence check: " + exists);

        Roles operation = Roles.UPLOAD_FILE;
        Optional<UserRole> userRole = userRoleRepository.findByUser_UserUuidAndRole(userUuid, operation);
        userRole.ifPresent(role -> System.out.println("User Role found: " + role));
    }
}
