package com.duongw.chatapp.controller;


import com.duongw.chatapp.config.AppConstant;
import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.user.ChangePasswordRequest;
import com.duongw.chatapp.model.dto.request.user.UserProfileUpdateRequest;
import com.duongw.chatapp.model.dto.request.usersetting.UserSettingsUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.model.dto.response.usersetting.UserSettingsResponseDTO;
import com.duongw.chatapp.security.auth.AuthUserDetails;
import com.duongw.chatapp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/users/")
public class UserProfileController {

    private final IUserService userService;


    @GetMapping(path = "/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUserProfile(
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to get current user profile for: {}", currentUser.getUsername());
        UserResponseDTO user = userService.getUserByEmail(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping(path = "/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest updateRequest,
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to update profile for user: {}", currentUser.getUsername());

        // Security check: ensure the user can only update their own profile
        if (!currentUser.getUsername().equals(updateRequest.getEmail())) {
            log.warn("User {} attempted to update profile for {}",
                    currentUser.getUsername(), updateRequest.getEmail());
            throw new com.duongw.chatapp.exception.ForbiddenException(
                    "You can only update your own profile");
        }

        UserResponseDTO updatedUser = userService.updateUserProfile(updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }


    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to change password for user: {}", currentUser.getUsername());

        // Validate password confirmation
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        userService.changePassword(
                currentUser.getUsername(),
                changePasswordRequest);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/status/{status}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateStatus(
            @PathVariable("status") String status,
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to update status to {} for user: {}", status, currentUser.getUsername());

        UserResponseDTO user = userService.changeUserStatus(currentUser.getUser().getId(), status);
        return ResponseEntity.ok(ApiResponse.success(user));
    }



    //TODO: get user setting
    @GetMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponseDTO>> getUserSettings(
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to get settings for user: {}", currentUser.getUser().getEmail());
        UserSettingsResponseDTO settings = userService.getUserSettings(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(settings));
    }


    //TODO: update user setting

    @PutMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponseDTO>> updateUserSettings(
            @Valid @RequestBody UserSettingsUpdateRequest settingsRequest,
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to update settings for user: {}", currentUser.getUser().getEmail());

        UserSettingsResponseDTO settings = userService.updateUserSettings(
                currentUser.getUser().getId(), settingsRequest);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

}
