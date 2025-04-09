package com.duongw.chatapp.controller;


import com.duongw.chatapp.exception.BadRequestException;
import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.PasswordResetRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.security.auth.AuthUserDetails;
import com.duongw.chatapp.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthService authService;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserLoginRequest loginRequest) {
        log.info("REST request to authenticate user: {}", loginRequest.getEmail());

        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegisterRequest registerRequest) {
        log.info("REST request to register user: {}", registerRequest.getEmail());

        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(authResponse));
    }

    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("REST request to refresh token");

        AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestParam String refreshToken) {
        log.info("REST request to logout user");

        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


    /**
     * Logs out a user from all devices by revoking all their refresh tokens
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal AuthUserDetails currentUser) {
        log.info("REST request to logout user from all devices: {}", currentUser.getUsername());

        authService.logoutAll(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }



    // TODO: request password reset
    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestParam("email") String email) {
        log.info("REST request to request password reset for: {}", email);
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    //TODO: reset password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        log.info("REST request to reset password with token");
        authService.resetPassword(resetRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }




    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Callback(
            @PathVariable String provider,
            @RequestParam String code) {
        log.info("Processing OAuth2 callback from provider: {}", provider);
        AuthResponse authResponse = authService.processOAuth2Login(provider, code);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }



    @GetMapping("/oauth2/authorize/{provider}")
    public ResponseEntity<ApiResponse<String>> getAuthorizationUrl(@PathVariable String provider) {
        log.info("Getting authorization URL for provider: {}", provider);

        String authorizationUrl;
        if ("google".equalsIgnoreCase(provider)) {
            authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&redirect_uri=" + redirectUri + "/google" +
                    "&response_type=code" +
                    "&scope=email%20profile";
        } else if ("github".equalsIgnoreCase(provider)) {
            // GitHub auth URL configuration
            authorizationUrl = "...";
        } else {
            throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        }

        return ResponseEntity.ok(ApiResponse.success(authorizationUrl));
    }

    /**
     * Verifies a user's email address using the token from the verification email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        log.info("REST request to verify email with token");

        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Resends the verification email to a user
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@RequestParam("email") String email) {
        log.info("REST request to resend verification email to: {}", email);

        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }






}
