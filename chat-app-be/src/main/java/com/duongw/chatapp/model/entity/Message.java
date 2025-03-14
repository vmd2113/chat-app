package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.base.BaseIdentityEntity;
import com.duongw.chatapp.model.entity.id.MessageContentId;
import com.duongw.chatapp.model.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "messages")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Message extends BaseIdentityEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Users sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Message parent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(nullable = false, name = "deleted")
    private Boolean deleted = false;


    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MessageReaction> reactions = new HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MessageRead> reads = new HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<File> files = new HashSet<>();

    // Helper methods
    public void setMessageContent(String content, boolean encrypted, String metadata) {
        MessageContent messageContent = new MessageContent();
        MessageContentId id = new MessageContentId(this.getId(), this.getSentAt());
        messageContent.setId(id);
        messageContent.setMessage(this);
        messageContent.setContent(content);
        messageContent.setEncrypted(encrypted);
        messageContent.setMetadata(metadata);
    }

    // Helper method to mark message as read
    public void markAsRead(Users user) {
        // Check if already read
        for (MessageRead read : reads) {
            if (read.getUser().equals(user)) {
                return;
            }
        }

        // Add new read marker
        MessageRead readMarker = MessageRead.builder()
                .message(this)
                .user(user)
                .readAt(Instant.now())
                .build();
        this.reads.add(readMarker);
    }

    // Helper method to add reaction
    public void addReaction(Users user, String reaction) {
        // Check if reaction already exists
        for (MessageReaction existing : this.reactions) {
            if (existing.getUser().equals(user) && existing.getReaction().equals(reaction)) {
                return;
            }
        }

        // Add new reaction
        MessageReaction newReaction = MessageReaction.builder()
                .message(this)
                .user(user)
                .reaction(reaction)
                .build();
        this.reactions.add(newReaction);
    }

    // Helper method to remove reaction
    public void removeReaction(Users user, String reaction) {
        this.reactions.removeIf(r -> r.getUser().equals(user) && r.getReaction().equals(reaction));
    }

    public boolean isEdited() {
        return editedAt != null;
    }
}
