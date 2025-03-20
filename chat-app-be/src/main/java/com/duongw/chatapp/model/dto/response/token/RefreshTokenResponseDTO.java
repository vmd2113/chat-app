package com.duongw.chatapp.model.dto.response.token;

import com.duongw.chatapp.model.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

public class RefreshTokenResponseDTO {

    private Long id;

    private Long userId;

    private String token;

    private String deviceInfo;

    private String ipAddress;

    private Instant expiresAt;

    private Boolean revoked = false;

}
