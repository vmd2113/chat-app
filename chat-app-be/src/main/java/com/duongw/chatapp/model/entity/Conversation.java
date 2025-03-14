package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;



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
}
