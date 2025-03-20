package com.duongw.chatapp.service;

import com.duongw.chatapp.model.entity.RefreshToken;
import com.duongw.chatapp.model.entity.Users;
import jakarta.servlet.http.HttpServletRequest;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(Users user, HttpServletRequest request);
    RefreshToken verifyExpiration(String token);
    void revokeToken(String token) ;


    void revokeAllUserTokens(Users user);

    void cleanupExpiredTokens();




}
