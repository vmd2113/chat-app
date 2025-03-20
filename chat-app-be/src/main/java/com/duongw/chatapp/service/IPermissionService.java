package com.duongw.chatapp.service;

import com.duongw.chatapp.model.dto.request.permission.PermissionCreateRequest;
import com.duongw.chatapp.model.dto.request.permission.PermissionUpdateRequest;
import com.duongw.chatapp.model.dto.response.permission.PermissionResponseDTO;

import java.util.List;

public interface IPermissionService {

    List<PermissionResponseDTO> getAllPermissions();

    PermissionResponseDTO getPermissionById(Long id);

    PermissionResponseDTO createPermission(PermissionCreateRequest permissionCreateRequest);

    PermissionResponseDTO updatePermission(Long id, PermissionUpdateRequest permissionUpdateRequest);

    void deletePermission(Long id);


}
