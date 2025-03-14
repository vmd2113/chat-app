package com.duongw.chatapp.model.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageContentId implements Serializable {

    @Column(name = "message_id")
    private Long messageId;
    private Instant sentAt;
}
