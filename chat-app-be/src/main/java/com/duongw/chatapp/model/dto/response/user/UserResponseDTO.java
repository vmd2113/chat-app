package com.duongw.chatapp.model.dto.response.user;


import com.duongw.chatapp.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserResponseDTO {

    private String email;

    private String password;

    private String fullName;

    private String avatar;

    private Instant lastActive;

    private UserStatus status = UserStatus.OFFLINE;

    private String publicKey;

    private Boolean emailVerified = false;
}
