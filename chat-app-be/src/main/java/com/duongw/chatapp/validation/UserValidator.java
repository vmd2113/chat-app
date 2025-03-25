package com.duongw.chatapp.validation;

import com.duongw.chatapp.exception.AlreadyExistedException;
import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.exception.EmailAlreadyExistsException;
import com.duongw.chatapp.model.dto.request.user.*;
import com.duongw.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor

public class UserValidator {

    // Biểu thức chính quy cho email - RFC 5322 compliant
    private final String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private final UserRepository userRepository;

    public void validateDuplicateInfo(String email, Long excludeUserId) {
        log.info("USER-VALIDATOR -> validateDuplicateInfo");
        log.info("Validating duplicate info for user. ExcludeId: {}", excludeUserId);

        // check email

        if (email != null && !email.isEmpty()) {
            boolean exitedUserByEmail;
            if (excludeUserId != null) {
                exitedUserByEmail = userRepository.existsByEmailAndIdNot(email, excludeUserId);
            } else {
                exitedUserByEmail = userRepository.existsByEmail(email);
            }
            if (exitedUserByEmail) {
                log.info("Validate failed - Email already exists");
                throw new EmailAlreadyExistsException(email);
            }

        }

    }

    private boolean validatePassword(String password) {
        return Pattern.matches(PASSWORD_PATTERN, password);
    }

    private boolean validateEmail(String email) {
        return Pattern.matches(EMAIL_PATTERN, email);
    }

    public void validateRegisterUser(UserRegisterRequest request) {
        try {
            validateDuplicateInfo(request.getEmail(), null);
            if (!validatePassword(request.getPassword())) {
                throw new BadRequestException("Password must contain at least 8 characters including uppercase, lowercase, numbers and special characters");
            }
            if (!validateEmail(request.getEmail())) {
                throw new BadRequestException("Invalid email format");
            }

        } catch (AlreadyExistedException e) {
            throw new AlreadyExistedException(e.getMessage());
        }

    }


    public void validateLoginUser(UserLoginRequest userLoginRequest) {
        try {

            if (!validateEmail(userLoginRequest.getEmail())) {
                throw new BadRequestException("Invalid email format");
            }
            if (userLoginRequest.getPassword() == null || userLoginRequest.getPassword().isEmpty()) {
                throw new BadRequestException("Password cannot be empty");
            }
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }

    }

    public void updateUserInformation(UserProfileUpdateRequest updateRequest, Long userId) {
        log.info("USER-VALIDATOR -> updateUserInformation");

        // Check for email changes (if applicable)
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            validateDuplicateInfo(updateRequest.getEmail(), userId);

            if (!validateEmail(updateRequest.getEmail())) {
                throw new BadRequestException("Invalid email format");
            }
        }

        if (updateRequest.getFullName() != null && updateRequest.getFullName().isEmpty()) {
            throw new BadRequestException("Full name cannot be empty");
        }
    }


    // method for admin


    public void validateCreateUser(UserCreateRequest userCreateRequest) {
        try {
            validateDuplicateInfo(userCreateRequest.getEmail(), null);
            if (!validatePassword(userCreateRequest.getPassword())) {
                throw new BadRequestException("Password must contain at least 8 characters including uppercase, lowercase, numbers and special characters");
            }
        } catch (AlreadyExistedException e) {
            throw new AlreadyExistedException(e.getMessage());
        }
    }

    public void validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        // Check if new password is empty
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BadRequestException("New password cannot be empty");
        }

        // Check if new password meets requirements
        if (!validatePassword(newPassword)) {
            throw new BadRequestException("Password must contain at least 8 characters including uppercase, lowercase, numbers and special characters");
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        // Current password cannot be the same as new password
        if (currentPassword.equals(newPassword)) {
            throw new BadRequestException("New password must be different from current password");
        }
    }

    public void validateUpdateUser(UserUpdateRequest userUpdateRequest, Long excludeUserId) {
        try {
            validateDuplicateInfo(userUpdateRequest.getEmail(), excludeUserId);
        } catch (AlreadyExistedException e) {
            throw new AlreadyExistedException(e.getMessage());
        }
    }


}
