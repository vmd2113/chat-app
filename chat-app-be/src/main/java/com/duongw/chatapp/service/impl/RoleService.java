package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.mapper.RoleMapper;
import com.duongw.chatapp.model.dto.request.role.RoleCreateRequest;
import com.duongw.chatapp.model.dto.request.role.RoleUpdateRequest;
import com.duongw.chatapp.model.dto.response.role.RoleResponseDTO;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.repository.RoleRepository;
import com.duongw.chatapp.service.IRoleService;
import com.duongw.chatapp.validation.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j

public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RoleValidator roleValidator;

    @Autowired
    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper, RoleValidator roleValidator) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.roleValidator = roleValidator;
    }


    private Role getById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toListDto(roles);
    }

    @Override
    public RoleResponseDTO getRoleById(Long id) {
        Role role = getById(id);
        return roleMapper.toDto(role);
    }

    @Transactional
    @Override
    public RoleResponseDTO createRole(RoleCreateRequest role) {
        roleValidator.validateRoleName(role.getName(), null);
        Role newRole = Role.builder()
                .name(role.getName())
                .description(role.getDescription())
                .build();
        roleRepository.save(newRole);
        return roleMapper.toDto(newRole);
    }

    @Transactional
    @Override
    public RoleResponseDTO updateRole(Long id, RoleUpdateRequest role) {
        Role oldRole = getById(id);
        roleValidator.validateRoleName(role.getName(), id);
        oldRole.setName(role.getName());
        oldRole.setDescription(role.getDescription());
        roleRepository.save(oldRole);
        return roleMapper.toDto(oldRole);

    }

    @Transactional
    @Override
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role != null) {
            roleRepository.delete(role);
        }

    }
}
