package com.duongw.chatapp.service;

import com.duongw.chatapp.model.dto.request.role.RoleCreateRequest;
import com.duongw.chatapp.model.dto.request.role.RoleUpdateRequest;
import com.duongw.chatapp.model.dto.response.role.RoleResponseDTO;
import com.duongw.chatapp.model.entity.Role;

import java.util.List;

public interface IRoleService {

    List<RoleResponseDTO> getAllRoles();

    RoleResponseDTO getRoleById(Long id);

    RoleResponseDTO createRole(RoleCreateRequest role);

    RoleResponseDTO updateRole(Long id, RoleUpdateRequest role);

    void deleteRole(Long id);

}
