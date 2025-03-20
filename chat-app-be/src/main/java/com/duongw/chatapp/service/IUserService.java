package com.duongw.chatapp.service;

import com.duongw.chatapp.model.dto.request.user.UserCreateRequest;
import com.duongw.chatapp.model.dto.request.user.UserProfileUpdateRequest;
import com.duongw.chatapp.model.dto.request.user.UserUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;

import java.util.List;

public interface IUserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO createUser(UserCreateRequest userCreateRequest);

    UserResponseDTO updateUser(UserUpdateRequest userUpdateRequest);

    UserResponseDTO updateUserProfile(UserProfileUpdateRequest userProfileUpdateRequest);

    UserResponseDTO changeUserStatus(Long userId, String status);

    UserResponseDTO changeUserEmailVerified(Long userId, Boolean emailVerified);

    void deleteUser(Long userId);

}
