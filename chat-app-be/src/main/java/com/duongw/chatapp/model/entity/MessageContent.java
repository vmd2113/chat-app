package com.duongw.chatapp.model.entity;

import com.duongw.chatapp.model.entity.id.MessageContentId;
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

    @EmbeddedId
    private MessageContentId id;

    @MapsId("messageId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    private String content;

    private Boolean encrypted = false;

    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;
}
