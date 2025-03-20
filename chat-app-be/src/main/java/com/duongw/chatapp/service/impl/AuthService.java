package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.exception.InvalidTokenException;
import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.model.entity.RefreshToken;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.RoleRepository;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.security.auth.AuthUserDetails;
import com.duongw.chatapp.security.token.JwtTokenProvider;
import com.duongw.chatapp.service.IAuthService;
import com.duongw.chatapp.service.IRefreshTokenService;
import com.duongw.chatapp.service.IRolePermissionService;
import com.duongw.chatapp.service.IUserRoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final IUserRoleService userRoleService;
    private final IRolePermissionService rolePermissionService;
    private final IRefreshTokenService refreshTokenService;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public AuthResponse login(UserLoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getEmail());

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
            Users user = userDetails.getUser();

            // Generate JWT token
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, request);

            // Extract roles and permissions
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            List<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            // Build and return response
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000) // Convert to seconds
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse register(UserRegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getEmail());

        try {
            // Check if passwords match
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                throw new BadRequestException("Passwords do not match");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }

            // Create new user
            Users user = Users.builder()
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .fullName(registerRequest.getFullName())
                    .status(UserStatus.OFFLINE) // Pending email verification
                    .emailVerified(false) // Requires email verification
                    .build();

            // Save user
            user = userRepository.save(user);

            // Assign default role (ROLE_USER)
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
            userRoleService.assignRoleToUser(user, userRole);

            // Generate JWT
            String accessToken = jwtTokenProvider.generateTokenFromMail(user.getEmail());

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, request);

            // Get role and permission names
            List<Role> roles = userRoleService.getRolesForUser(user);
            List<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            List<String> permissionNames = roles.stream()
                    .flatMap(role -> rolePermissionService.getPermissionsForRole(role).stream())
                    .map(Permission::getName)
                    .distinct()
                    .collect(Collectors.toList());

            // Build and return response
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roleNames)
                    .permissions(permissionNames)
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registration failed for email: {}", registerRequest.getEmail(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("Processing refresh token request");

        try {
            // Verify refresh token
            RefreshToken refreshToken = refreshTokenService.verifyExpiration(refreshTokenRequest.getRefreshToken());
            Users user = refreshToken.getUser();

            // Generate new access token
            String accessToken = jwtTokenProvider.generateTokenFromMail(user.getEmail());

            // Get role and permission names
            List<Role> roles = userRoleService.getRolesForUser(user);
            List<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            List<String> permissionNames = roles.stream()
                    .flatMap(role -> rolePermissionService.getPermissionsForRole(role).stream())
                    .map(Permission::getName)
                    .distinct()
                    .collect(Collectors.toList());

            // Build and return response
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken()) // Return same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .roles(roleNames)
                    .permissions(permissionNames)
                    .build();

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Processing logout request");

        try {
            // Revoke refresh token
            refreshTokenService.revokeToken(refreshToken);
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        log.info("Processing logout from all devices for user ID: {}", userId);

        try {
            // Find user
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            // Revoke all refresh tokens for the user
            refreshTokenService.revokeAllUserTokens(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout from all devices failed for user ID: {}", userId, e);
            throw new RuntimeException("Logout from all devices failed: " + e.getMessage(), e);
        }
    }
}