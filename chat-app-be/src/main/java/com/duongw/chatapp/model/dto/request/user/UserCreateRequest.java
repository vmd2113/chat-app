package com.duongw.chatapp.model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserCreateRequest {

    private String email;

    private String password;

    private String fullName;

    private String avatar;

    private Instant lastActive;

    private String publicKey;

    private Boolean emailVerified = true;
}
