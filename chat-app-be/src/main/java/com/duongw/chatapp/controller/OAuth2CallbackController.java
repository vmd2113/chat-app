package com.duongw.chatapp.controller;

import com.duongw.chatapp.model.base.ApiResponse;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;
import com.duongw.chatapp.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/callback")
@RequiredArgsConstructor

@Slf4j
public class OAuth2CallbackController {

    private final IAuthService authService;

    @GetMapping("/google")
    public ResponseEntity<ApiResponse<?>> handleGoogleCallback(@RequestParam("code") String code) {
        log.info("Received Google callback with code: {}", code);

        try {
            // Process the OAuth2 code and generate tokens
            AuthResponse authResponse = authService.processOAuth2Login("google", code);

            // Instead of returning JSON, redirect to the frontend with tokens
            // For a quick test, you can just return the AuthResponse
            return ResponseEntity.ok(ApiResponse.success(authResponse));

            // In a real application, you would redirect to your frontend
            // URI frontendUri = URI.create("http://localhost:5173/login/success?token=" + authResponse.getAccessToken());
            // return ResponseEntity.status(HttpStatus.FOUND).location(frontendUri).build();
        } catch (Exception e) {
            log.error("Error processing Google callback", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(String.valueOf(500), e.getMessage()));
        }
    }
}