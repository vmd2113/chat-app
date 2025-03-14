package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
})

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Friendship extends BaseIdentityEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "requester_id", nullable = false)
    private Users requester;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private Users addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus friendshipStatus = FriendshipStatus.PENDING;
}
