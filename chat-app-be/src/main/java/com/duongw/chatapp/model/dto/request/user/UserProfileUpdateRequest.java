package com.duongw.chatapp.model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserProfileUpdateRequest {
    private String email;

    private String fullName;

    private String avatar;


}
