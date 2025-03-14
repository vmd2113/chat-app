package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.enums.ConversationType;
import com.duongw.chatapp.model.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "conversations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class Conversation extends BaseIdentityEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ConversationType conversationType;

    @Column(name = "name")
    private String conversationName;


    @Column(name = "avatar")
    private String avatar;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = true;

    // In Conversation.java
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // In Conversation.java
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConversationParticipant> participants = new HashSet<>();


    public void addParticipant(Users user, ParticipantRole role) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(this);
        participant.setUsers(user);
        participant.setRole(role);
        this.participants.add(participant);
    }

    public void removeParticipant(Users user) {
        this.participants.removeIf(participant -> participant.getUsers().equals(user));
    }

    public boolean isGroupConversation() {
        return ConversationType.GROUP.equals(this.conversationType);
    }

    public boolean isIndividualConversation() {
        return ConversationType.INDIVIDUAL.equals(this.conversationType);
    }
}
