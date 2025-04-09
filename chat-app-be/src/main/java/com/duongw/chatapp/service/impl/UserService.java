package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.exception.InvalidTokenException;
import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.mapper.UserMapper;
import com.duongw.chatapp.model.dto.mapper.UserSettingMapper;
import com.duongw.chatapp.model.dto.request.user.*;
import com.duongw.chatapp.model.dto.request.usersetting.UserSettingsUpdateRequest;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.model.dto.response.usersetting.UserSettingsResponseDTO;
import com.duongw.chatapp.model.entity.ResetToken;
import com.duongw.chatapp.model.entity.UserSettings;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.ResetTokenRepository;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.repository.UserSettingRepository;
import com.duongw.chatapp.repository.UserStatusLogRepository;
import com.duongw.chatapp.service.IUserService;
import com.duongw.chatapp.service.email.EmailService;
import com.duongw.chatapp.utils.StringUtil;
import com.duongw.chatapp.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    private final UserStatusService userStatusService;
    private final ResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final UserSettingRepository userSettingRepository;

    private final UserSettingMapper userSettingMapper;

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



    @Transactional
    @Override
    public void requestPasswordReset(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Generate reset token
        String token = UUID.randomUUID().toString();

        // Save token (create reset token entity and repository)
        ResetToken resetToken = ResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        resetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = "http://localhost:/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }

    /**
     * Resets password using a valid reset token
     * @param resetRequest Contains token and new password
     */
    @Transactional
    @Override
    public void resetPassword(PasswordResetRequest resetRequest) {
        // Validate token
        ResetToken resetToken = resetTokenRepository.findByToken(resetRequest.getToken())
                .orElseThrow(() -> new InvalidTokenException("Reset token not found"));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired");
        }

        // Validate new password
        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Update password
        Users user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        userRepository.save(user);

        // Delete token
        resetTokenRepository.delete(resetToken);

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user);
    }

    /**
     * Updates user status and logs activity
     * @param userId User ID
     * @param status New status
     * @return Updated user
     */
    @Transactional
    @Override
    public UserResponseDTO updateUserStatus(Long userId, UserStatus status) {
        Users user = findById(userId);

        // Update status
        user.setStatus(status);
        user.setLastActive(Instant.now());
        userRepository.save(user);

        // Log status change
        userStatusService.logStatusChange(userId, status);

        return userMapper.toDto(user);
    }

    /**
     * Retrieves user settings
     * @param userId User ID
     * @return User settings
     */
    @Override
    public UserSettingsResponseDTO getUserSettings(Long userId) {
        Users user = findById(userId);
        UserSettings settings = user.getSettings();

        if (settings == null) {
            // Create default settings if they don't exist
            settings = UserSettings.builder()
                    .user(user)
                    .notificationEnabled(true)
                    .notificationSound(true)
                    .showStatus(true)
                    .language("en")
                    .theme("light")
                    .build();

            userSettingRepository.save(settings);
            user.setSettings(settings);
        }

        return userSettingMapper.toDto(settings);
    }

    /**
     * Updates user settings
     * @param userId User ID
     * @param settingsRequest New settings
     * @return Updated settings
     */
    @Transactional
    @Override
    public UserSettingsResponseDTO updateUserSettings(Long userId, UserSettingsUpdateRequest settingsRequest) {
        Users user = findById(userId);
        UserSettings settings = user.getSettings();

        if (settings == null) {
            settings = new UserSettings();
            settings.setUser(user);
        }

        // Update settings
        if (settingsRequest.getNotificationEnabled() != null) {
            settings.setNotificationEnabled(settingsRequest.getNotificationEnabled());
        }

        if (settingsRequest.getNotificationSound() != null) {
            settings.setNotificationSound(settingsRequest.getNotificationSound());
        }

        if (settingsRequest.getShowStatus() != null) {
            settings.setShowStatus(settingsRequest.getShowStatus());
        }

        if (settingsRequest.getLanguage() != null) {
            settings.setLanguage(settingsRequest.getLanguage());
        }

        if (settingsRequest.getTheme() != null) {
            settings.setTheme(settingsRequest.getTheme());
        }

        userSettingRepository.save(settings);
        return userSettingMapper.toDto(settings);
    }
}
