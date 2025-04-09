package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.*;
import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.PasswordResetRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.model.entity.*;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.*;
import com.duongw.chatapp.security.auth.AuthUserDetails;
import com.duongw.chatapp.security.token.JwtTokenProvider;
import com.duongw.chatapp.service.*;
import com.duongw.chatapp.service.email.EmailService;
import com.duongw.chatapp.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final ResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final HttpServletRequest request;
    private final IOAuth2Service oAuth2Service;
    private final UserOauthRepository userOauthRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${app.verification.expiry-hours}")
    private int verificationExpiryHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;



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

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", loginRequest.getEmail());
            throw new BadRequestException("Invalid email or password");
        } catch (DisabledException e) {
            log.error("Account disabled for user: {}", loginRequest.getEmail());
            throw new UnauthorizedException("Account is disabled");
        } catch (LockedException e) {
            log.error("Account locked for user: {}", loginRequest.getEmail());
            throw new UnauthorizedException("Account is locked");
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw new UnauthorizedException("Authentication failed: " + e.getMessage());
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


            String token = StringUtil.generateUUID();
            VerificationToken verificationToken = VerificationToken.builder()
                    .token(token)
                    .users(user)
                    .expiryDate(Instant.now().plus(verificationExpiryHours, ChronoUnit.HOURS))
                    .build();

            verificationTokenRepository.save(verificationToken);
            // Send verification email

            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);

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

    @Transactional
    public void requestPasswordReset(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Generate reset token
        String token = StringUtil.generateUUID();

        // Save token (create reset token entity and repository)
        ResetToken resetToken = ResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        resetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }

    /**
     * Resets password using a valid reset token
     * @param resetRequest Contains token and new password
     */
    @Transactional
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

    @Override
    public void verifyEmail(String token) {

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Verification token", "token", token));

        if (verificationToken.isExpired()){
            throw new InvalidTokenException("Verification token has expired");
        }

        Users users = verificationToken.getUsers();

        users.setEmailVerified(true);
        userRepository.save(users);
        verificationTokenRepository.delete(verificationToken);

    }

    @Override
    public void resendVerificationEmail(String email) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Delete any existing tokens
        verificationTokenRepository.deleteByUsers(user);

        // Create new token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .users(user)
                .expiryDate(Instant.now().plus(verificationExpiryHours, ChronoUnit.HOURS))
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);

    }
}