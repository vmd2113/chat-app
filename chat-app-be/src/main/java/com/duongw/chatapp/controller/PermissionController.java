package com.duongw.chatapp.controller;


import com.duongw.chatapp.config.AppConstant;
import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.permission.PermissionCreateRequest;
import com.duongw.chatapp.model.dto.request.permission.PermissionUpdateRequest;
import com.duongw.chatapp.model.dto.response.permission.PermissionResponseDTO;
import com.duongw.chatapp.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/admin/permissions")

public class PermissionController {

    private final IPermissionService permissionService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getAllPermissions() {
        List<PermissionResponseDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> getPermissionById(@PathVariable(name = "id") Long id) {
        PermissionResponseDTO permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> createPermission(@RequestBody PermissionCreateRequest permissionCreateRequest) {
        PermissionResponseDTO permission = permissionService.createPermission(permissionCreateRequest);
        return ResponseEntity.ok(ApiResponse.created(permission));
    }


    @PutMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> updatePermission(@PathVariable(name = "id") Long id, @RequestBody PermissionUpdateRequest permissionUpdateRequest) {
        PermissionResponseDTO permission = permissionService.updatePermission(id, permissionUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable(name = "id") Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

}
