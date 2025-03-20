package com.duongw.chatapp.controller;


import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthService authService;

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




}
