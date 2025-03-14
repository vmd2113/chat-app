package com.duongw.chatapp.model.entity;


import com.duongw.chatapp.model.base.BaseIdentityEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friend_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class FriendGroup extends BaseIdentityEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private String name;

}
