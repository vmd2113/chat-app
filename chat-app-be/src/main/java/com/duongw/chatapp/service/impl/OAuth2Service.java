package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.exception.OAuthException;
import com.duongw.chatapp.service.IOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor

public class OAuth2Service implements IOAuth2Service {

    private final RestTemplate restTemplate;


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;


    @Override
    public Map<String, Object> getUserAttributes(String provider, String code) {
        if ("google".equalsIgnoreCase(provider)) {
            return getGoogleUserAttributes(code);
        } else if ("github".equalsIgnoreCase(provider)) {
            return getGithubUserAttributes(code);
        } else {
            throw new OAuthException("Unsupported OAuth2 provider: " + provider);
        }
    }

    private Map<String, Object> getGoogleUserAttributes(String code) {

        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

            body.set("code", code);
            body.set("client_id", googleClientId);
            body.set("client_secret", googleClientSecret);
            body.set("redirect_uri", redirectUri + "/google");
            body.set("grant_type", "authorization_code");


            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );
            Map<String, Object> userAttributes = userInfoResponse.getBody();
            log.info("Received user attributes from Google: {}", userAttributes);


            return userAttributes;
        } catch (Exception e) {
            log.error("Failed to get Google user attributes", e);
            throw new OAuthException("Failed to get Google user attributes: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getGithubUserAttributes(String code) {
        return Map.of();
    }
}
