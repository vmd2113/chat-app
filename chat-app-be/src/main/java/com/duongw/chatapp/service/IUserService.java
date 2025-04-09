package com.duongw.chatapp.service;

import com.duongw.chatapp.model.dto.request.user.*;
import com.duongw.chatapp.model.dto.request.usersetting.UserSettingsUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.model.dto.response.usersetting.UserSettingsResponseDTO;
import com.duongw.chatapp.model.enums.UserStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IUserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO createUser(UserCreateRequest userCreateRequest);

    UserResponseDTO updateUser(UserUpdateRequest userUpdateRequest);

    UserResponseDTO updateUserProfile(UserProfileUpdateRequest userProfileUpdateRequest);

    UserResponseDTO changeUserStatus(Long userId, String status);

    @Transactional
    void changePassword(String email, ChangePasswordRequest changePasswordRequest);

    UserResponseDTO changeUserEmailVerified(Long userId, Boolean emailVerified);

    void deleteUser(Long userId);

    void requestPasswordReset(String email);

    void resetPassword (PasswordResetRequest request);

    UserSettingsResponseDTO getUserSettings(Long userId);

    UserSettingsResponseDTO updateUserSettings(Long userId, UserSettingsUpdateRequest userSettingsUpdateRequest);

    UserResponseDTO updateUserStatus(Long userId, UserStatus status);

}
