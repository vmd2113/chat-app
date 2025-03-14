package com.duongw.chatapp.model.entity;


import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Table(name = "users")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Users extends BaseIdentityEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;


    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;


    @Column(name = "avatar")
    private String avatar;

    @Column(name = "last_active")
    private Instant lastActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "public_key")
    private String publicKey;


    @Column(name = "email_verified")
    private Boolean emailVerified = false;


}
