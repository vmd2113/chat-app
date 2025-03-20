package com.duongw.chatapp.controller;


import com.duongw.chatapp.config.AppConstant;
import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.role.RoleCreateRequest;
import com.duongw.chatapp.model.dto.request.role.RoleUpdateRequest;
import com.duongw.chatapp.model.dto.response.role.RoleResponseDTO;
import com.duongw.chatapp.service.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/admin/roles")
public class RoleController {

    private final IRoleService roleService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getAllRoles() {
        List<RoleResponseDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(@PathVariable(name = "id") Long id) {
        RoleResponseDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<RoleResponseDTO>> createRole(@Valid @RequestBody RoleCreateRequest roleCreateRequest){
        RoleResponseDTO role = roleService.createRole(roleCreateRequest);
        return ResponseEntity.ok(ApiResponse.created(role));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(@PathVariable(name = "id") Long id, @Valid @RequestBody RoleUpdateRequest roleCreateRequest){
        RoleResponseDTO role = roleService.updateRole(id, roleCreateRequest);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable(name = "id") Long id){
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }


}
