package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.exception.InvalidTokenException;
import com.duongw.chatapp.exception.OAuthException;
import com.duongw.chatapp.exception.ResourceNotFoundException;
import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.model.entity.*;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.RoleRepository;
import com.duongw.chatapp.repository.UserOauthRepository;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.security.auth.AuthUserDetails;
import com.duongw.chatapp.security.token.JwtTokenProvider;
import com.duongw.chatapp.service.*;
import com.duongw.chatapp.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private final IOAuth2Service oAuth2Service;
    private final UserOauthRepository userOauthRepository;



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

                    .emailVerified(false) // Requires email verification
                    .build();

            // Save user

            // Thiết lập status sau khi đã tạo đối tượng user
            user.setStatus(UserStatus.OFFLINE);
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

    @Override
    @Transactional
    public AuthResponse processOAuth2Login(String provider, String code) {
        log.info("Processing OAuth2 login for provider: {}", provider);
        log.info("Code: {}", code);

        try {
            // Get user info from provider
            Map<String, Object> userAttributes = oAuth2Service.getUserAttributes(provider, code);

            // Extract email and name
            String email = (String) userAttributes.get("email");
            String name = (String) userAttributes.get("name");
            String providerId = (String) userAttributes.getOrDefault("sub",
                    userAttributes.getOrDefault("id", "unknown"));


            if (email == null) {
                throw new OAuthException("Email not provided by OAuth provider. Make sure you've requested email scope.");
            }

            if (name == null) {
                name = email.split("@")[0]; // Use part before @ as name
                log.info("Name not provided by OAuth provider, using derived name: {}", name);
            }
            // Check if user exists by OAuth2 provider details
            Users user = userOauthRepository.findByProviderAndProviderUserId(provider, providerId)
                    .map(UserOauth::getUser)
                    .orElse(null);

            // If not found, check by email
            if (user == null && email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }

            // Create new user if not exists
            if (user == null) {
                user = Users.builder()
                        .email(email)
                        .fullName(name)
                        .password(passwordEncoder.encode(StringUtil.generateUUID()))
                        .status(UserStatus.ONLINE)
                        .emailVerified(true) // OAuth2 providers verify emails
                        .build();
                user = userRepository.save(user);

                // Assign default role (ROLE_USER)
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
                userRoleService.assignRoleToUser(user, userRole);
            }

            // Link OAuth2 provider if not already linked
            if (userOauthRepository.findByProviderAndProviderUserId(provider, providerId).isEmpty()) {
                user.addOAuthConnection(provider, providerId);
                userRepository.save(user);
            }

            // Update status to online
            user.setStatus(UserStatus.ONLINE);
            user.setLastActive(Instant.now());
            userRepository.save(user);

            // Generate JWT token
            String accessToken = jwtTokenProvider.generateTokenFromMail(user.getEmail());

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, request);

            // Get roles and permissions
            List<Role> roles = userRoleService.getRolesForUser(user);
            List<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            List<String> permissionNames = roles.stream()
                    .flatMap(role -> rolePermissionService.getPermissionsForRole(role).stream())
                    .map(Permission::getName)
                    .distinct()
                    .collect(Collectors.toList());

            // Return response
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

        } catch (Exception e) {
            log.error("OAuth2 login failed for provider: {}", provider, e);
            throw new OAuthException("Failed to process OAuth2 login: " + e.getMessage(), e);
        }
    }
}