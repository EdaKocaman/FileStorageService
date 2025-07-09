package com.example.FileStorageService.service;

import com.example.FileStorageService.enums.Roles;
import com.example.FileStorageService.model.Role;
import com.example.FileStorageService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Optional<Role> getRoleByEnum(Roles roleEnum) {
        return roleRepository.findByRoleName(roleEnum);
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }
}
