package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);
    List<RolePermission> findByPermission(Permission permission);
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
    boolean existsByRoleAndPermission(Role role, Permission permission);
    void deleteByRoleAndPermission(Role role, Permission permission);
}
