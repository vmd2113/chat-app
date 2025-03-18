package com.duongw.chatapp.model.dto.response.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String fullName;
    private String email;


}
