package com.duongw.chatapp.model.entity;


import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


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


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FriendGroup> friendGroups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserOauth> oauthConnections = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSettings settings;


    public void addOAuthConnection(String provider, String providerUserId) {
        UserOauth oauthConnection = new UserOauth();
        oauthConnection.setUsers(this);
        oauthConnection.setProvider(provider);
        oauthConnection.setProviderUserId(providerUserId);
        this.oauthConnections.add(oauthConnection);
    }

    @PrePersist
    private void initializeSettings() {
        if (this.settings == null) {
            this.settings = UserSettings.builder()
                    .user(this)
                    .notificationEnabled(true)
                    .notificationSound(true)
                    .showStatus(true)
                    .language("vi")
                    .theme("light")
                    .build();
        }
    }

    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(this);
        userRole.setRole(role);
        userRoles.add(userRole);
    }

    public void removeRole(Role role) {
        userRoles.removeIf(userRole -> userRole.getRole().equals(role));
    }

    public boolean hasRole(String roleName) {
        return userRoles.stream()
                .anyMatch(userRole -> userRole.getRole().getName().equals(roleName));
    }


}
