package com.duongw.chatapp.model.entity;


import com.duongw.chatapp.model.base.BaseIdentityEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity

@Table(name = "friend_group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"friend_group_id", "friend_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class FriendGroupMember extends BaseIdentityEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Users friend;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_group_id", nullable = false)
    private FriendGroup friendGroup;



}
