package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_oauth", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOauth extends BaseIdentityEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
}
