package com.duongw.chatapp.controller;


import com.duongw.chatapp.config.AppConstant;
import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.user.UserCreateRequest;
import com.duongw.chatapp.model.dto.request.user.UserProfileUpdateRequest;
import com.duongw.chatapp.model.dto.request.user.UserUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/admin/users")
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        log.info("REST request to get all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable("id") Long id) {
        log.info("REST request to get user with ID: {}", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByEmail(
            @PathVariable("email") String email) {
        log.info("REST request to get user with email: {}", email);
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserCreateRequest userCreateRequest) {
        log.info("REST request to create user with email: {}", userCreateRequest.getEmail());
        UserResponseDTO user = userService.createUser(userCreateRequest);
        return ResponseEntity.ok(ApiResponse.created(user));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        log.info("REST request to update user with email: {}", userUpdateRequest.getEmail());
        UserResponseDTO user = userService.updateUser(userUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest userProfileUpdateRequest) {
        log.info("REST request to update profile for user with email: {}", userProfileUpdateRequest.getEmail());
        UserResponseDTO user = userService.updateUserProfile(userProfileUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(user));
    }


    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeUserStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") String status) {
        log.info("REST request to change status to {} for user with ID: {}", status, id);
        UserResponseDTO user = userService.changeUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
//
//    @PutMapping("/{id}/email-verified/{verified}")
//    public ResponseEntity<ApiResponse<UserResponseDTO>> changeEmailVerifiedStatus(
//            @PathVariable("id") Long id,
//            @PathVariable("verified") Boolean verified) {
//        log.info("REST request to change email verification status to {} for user with ID: {}", verified, id);
//        UserResponseDTO user = userService.changeUserEmailVerified(id, verified);
//        return ResponseEntity.ok(ApiResponse.success(user));
//    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable("id") Long id) {
        log.info("REST request to delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }


}
