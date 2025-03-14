package com.duongw.chatapp.model.entity;


import com.duongw.chatapp.model.base.BaseIdentityEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "friendGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FriendGroupMember> members = new HashSet<>();

    public void addMember(Users friend) {
        FriendGroupMember member = new FriendGroupMember();
        member.setFriendGroup(this);
        member.setFriend(friend);
        this.members.add(member);
    }

    // Helper method to remove member
    public void removeMember(Users friend) {
        this.members.removeIf(member -> member.getFriend().equals(friend));
    }

}
