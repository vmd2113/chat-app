package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "encryption_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptionKey extends BaseIdentityEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(name = "key_type", nullable = false)
    private String keyType;

    @Column(name = "key_value", nullable = false)
    private String keyValue;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isExpired();
    }
}