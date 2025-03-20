package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.RolePermission;
import com.duongw.chatapp.repository.PermissionRepository;
import com.duongw.chatapp.repository.RolePermissionRepository;
import com.duongw.chatapp.repository.RoleRepository;
import com.duongw.chatapp.service.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionService implements IRolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<Permission> getPermissionsForRole(Role role) {
        return rolePermissionRepository.findByRole(role).stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesWithPermission(Permission permission) {
        return rolePermissionRepository.findByPermission(permission).stream()
                .map(RolePermission::getRole)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissionToRole(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);

        if (roleOpt.isPresent() && permissionOpt.isPresent()) {
            assignPermissionToRole(roleOpt.get(), permissionOpt.get());
        }
    }

    @Transactional
    public void removePermissionFromRole(Role role, Permission permission) {
        rolePermissionRepository.deleteByRoleAndPermission(role, permission);
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);

        if (roleOpt.isPresent() && permissionOpt.isPresent()) {
            removePermissionFromRole(roleOpt.get(), permissionOpt.get());
        }
    }

    @Transactional
    public void assignPermissionsToRole(Role role, Set<Permission> permissions) {
        permissions.forEach(permission -> assignPermissionToRole(role, permission));
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            permissionIds.forEach(permissionId -> {
                permissionRepository.findById(permissionId)
                        .ifPresent(permission -> assignPermissionToRole(role, permission));
            });
        }
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Role role, String permissionName) {
        Optional<Permission> permission = permissionRepository.findByName(permissionName);
        return permission.map(p -> rolePermissionRepository.existsByRoleAndPermission(role, p))
                .orElse(false);
    }

}
