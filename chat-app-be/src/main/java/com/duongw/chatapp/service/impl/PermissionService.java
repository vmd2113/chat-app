package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.mapper.PermissionMapper;
import com.duongw.chatapp.model.dto.request.permission.PermissionCreateRequest;
import com.duongw.chatapp.model.dto.request.permission.PermissionUpdateRequest;
import com.duongw.chatapp.model.dto.response.permission.PermissionResponseDTO;
import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.repository.PermissionRepository;
import com.duongw.chatapp.service.IPermissionService;
import com.duongw.chatapp.validation.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j

@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final PermissionValidator permissionValidator;


    private Permission findById(Long permissionId) {
        return permissionRepository.findById(permissionId).orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));
    }

    @Override
    public List<PermissionResponseDTO> getAllPermissions() {
        List<Permission> permissionsList = permissionRepository.findAll();
        return permissionMapper.toListDto(permissionsList);
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long id) {
        Permission permission = findById(id);
        return permissionMapper.toDto(permission);
    }

    @Transactional
    @Override
    public PermissionResponseDTO createPermission(PermissionCreateRequest permissionCreateRequest) {
        permissionValidator.validatePermissionName(permissionCreateRequest.getName(), null);

        Permission permission = Permission.builder()
                .name(permissionCreateRequest.getName())
                .description(permissionCreateRequest.getDescription())
                .build();

        permission = permissionRepository.save(permission);
        return permissionMapper.toDto(permission);
    }

    @Transactional
    @Override
    public PermissionResponseDTO updatePermission(Long id, PermissionUpdateRequest permissionUpdateRequest) {
        Permission permission = findById(id);
        permissionValidator.validatePermissionName(permissionUpdateRequest.getName(), id);

        permission.setName(permissionUpdateRequest.getName());
        permission.setDescription(permissionUpdateRequest.getDescription());

        permission = permissionRepository.save(permission);
        return permissionMapper.toDto(permission);


    }

    @Transactional
    @Override
    public void deletePermission(Long id) {
        Permission permission = findById(id);
        if (permission != null) {
            permissionRepository.delete(permission);
        }

    }
}
