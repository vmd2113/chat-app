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
        log.info("Received Google callback with code");
        return processOAuthCallback("google", code);
    }

    @GetMapping("/github")
    public ResponseEntity<ApiResponse<?>> handleGithubCallback(@RequestParam("code") String code) {
        log.info("Received GitHub callback with code");
        return processOAuthCallback("github", code);
    }

    private ResponseEntity<ApiResponse<?>> processOAuthCallback(String provider, String code) {
        try {
            AuthResponse authResponse = authService.processOAuth2Login(provider, code);

            // For API response
            return ResponseEntity.ok(ApiResponse.success(authResponse));

            // For frontend redirect (uncomment if needed)
            /*
            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/callback")
                    .queryParam("token", authResponse.getAccessToken())
                    .queryParam("refreshToken", authResponse.getRefreshToken())
                    .build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
            */
        } catch (Exception e) {
            log.error("Error processing OAuth callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OAUTH_ERROR", e.getMessage()));
        }
    }
}