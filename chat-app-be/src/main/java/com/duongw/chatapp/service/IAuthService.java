package com.duongw.chatapp.service;

import com.duongw.chatapp.model.dto.request.token.RefreshTokenRequest;
import com.duongw.chatapp.model.dto.request.user.PasswordResetRequest;
import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;
import com.duongw.chatapp.model.dto.response.auth.AuthResponse;

public interface IAuthService {

    AuthResponse login(UserLoginRequest loginRequest);

    AuthResponse register(UserRegisterRequest registerRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String refreshToken);

    void logoutAll(Long userId);

    void requestPasswordReset(String email);

    void resetPassword (PasswordResetRequest request);

    AuthResponse processOAuth2Login(String provider, String code);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}