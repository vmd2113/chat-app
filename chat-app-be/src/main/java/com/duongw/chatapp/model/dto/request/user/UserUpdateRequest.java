package com.duongw.chatapp.model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;


@Data
@Builder
@AllArgsConstructor

public class UserUpdateRequest {

    private String email;

    private String password;

    private String fullName;

    private String avatar;

    private Instant lastActive;

    private String publicKey;

}
