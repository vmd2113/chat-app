package com.duongw.chatapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MessageContent {

    @Id
    @Column(name = "message_id")
    private Long messageId;

    @Id
    @Column(name = "sent_at")
    private Instant sentAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @Column
    private String content;

    @Column(nullable = false)
    private Boolean encrypted = false;

    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    // Inner class for composite key
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MessageContentId implements Serializable {
        private Long messageId;
        private Instant sentAt;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageContentId that = (MessageContentId) o;
            return Objects.equals(messageId, that.messageId) && Objects.equals(sentAt, that.sentAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, sentAt);
        }
    }
}
