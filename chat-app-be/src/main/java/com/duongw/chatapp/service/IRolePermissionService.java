package com.duongw.chatapp.service;

import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.RolePermission;

import java.util.List;
import java.util.Set;

public interface IRolePermissionService {

    List<Permission> getPermissionsForRole(Role role);

    List<Role> getRolesWithPermission(Permission permission);

    void assignPermissionToRole(Role role, Permission permission);

    void assignPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Role role, Permission permission);

    void removePermissionFromRole(Long roleId, Long permissionId);

    void assignPermissionsToRole(Role role, Set<Permission> permissions);

    void assignPermissionsToRole(Long roleId, Set<Long> permissionIds);

    boolean hasPermission(Role role, String permissionName);


}