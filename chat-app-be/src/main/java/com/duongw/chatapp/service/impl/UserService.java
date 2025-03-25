package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.mapper.UserMapper;
import com.duongw.chatapp.model.dto.request.user.ChangePasswordRequest;
import com.duongw.chatapp.model.dto.request.user.UserCreateRequest;
import com.duongw.chatapp.model.dto.request.user.UserProfileUpdateRequest;
import com.duongw.chatapp.model.dto.request.user.UserUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.service.IUserService;
import com.duongw.chatapp.utils.StringUtil;
import com.duongw.chatapp.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("USER-SERVICE -> getAllUsers");
        List<Users> usersList = userRepository.findAll();
        return userMapper.toListDto(usersList);
    }


    private Users findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        log.info("USER-SERVICE -> getUserById");
        Users user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        log.info("USER-SERVICE -> getUserByEmail");
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserResponseDTO createUser(UserCreateRequest userCreateRequest) {
        log.info("USER-SERVICE -> createUser");
        userValidator.validateCreateUser(userCreateRequest);
        Users user = Users.builder()
                .email(userCreateRequest.getEmail())
                .avatar(userCreateRequest.getAvatar())
                .password(userCreateRequest.getPassword())
                .fullName(userCreateRequest.getFullName())
                .publicKey(userCreateRequest.getPublicKey())
                .lastActive(null)
                .status(null)
                .build();

        return userMapper.toDto(userRepository.save(user));

    }

    @Transactional
    @Override
    public UserResponseDTO updateUser(UserUpdateRequest userUpdateRequest) {
        log.info("USER-SERVICE -> updateUser");

        Users user = Users.builder()
                .email(userUpdateRequest.getEmail())
                .avatar(userUpdateRequest.getAvatar())
                .fullName(userUpdateRequest.getFullName())
                .password(userUpdateRequest.getPassword())
                .avatar(userUpdateRequest.getAvatar())
                .publicKey(userUpdateRequest.getPublicKey())
                .lastActive(null)
                .status(null)
                .build();
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponseDTO updateUserProfile(UserProfileUpdateRequest updateRequest) {
        log.info("USER-SERVICE -> updateUserProfile for email: {}", updateRequest.getEmail());

        // Find the user
        Users user = userRepository.findByEmail(updateRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", updateRequest.getEmail()));

        // Validate update data
        userValidator.updateUserInformation(updateRequest, user.getId());

        // Update fields if provided
        if (StringUtil.isNotNullOrEmpty(updateRequest.getFullName())) {
            user.setFullName(updateRequest.getFullName());
        }

        if (StringUtil.isNotNullOrEmpty(updateRequest.getAvatar())) {
            user.setAvatar(updateRequest.getAvatar());
        }

        // Save the updated user
        Users updatedUser = userRepository.save(user);

        // Return the updated user DTO
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @Override
    public UserResponseDTO changeUserStatus(Long userId, String status) {
        log.info("USER-SERVICE -> changeUserStatus");
        Users user = findById(userId);
        if (user != null) {
            user.setStatus(UserStatus.valueOf(status));
            userRepository.save(user);
        }
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        log.info("Changing password for user: {}", email);


        userValidator.validatePasswordChange(changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmPassword());
        // Find user
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));



        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);

        // Optionally: Revoke all refresh tokens to force re-login on all devices
        refreshTokenService.revokeAllUserTokens(user);
    }

    // TODO: Implement changeUserEmailVerified
    @Override
    public UserResponseDTO changeUserEmailVerified(Long userId, Boolean emailVerified) {
        log.info("USER-SERVICE -> changeUserEmailVerified");
        Users user = findById(userId);
        if (user != null) {
            user.setEmailVerified(emailVerified);
            userRepository.save(user);
        }
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        log.info("USER-SERVICE -> deleteUser");
        Users user = findById(userId);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
