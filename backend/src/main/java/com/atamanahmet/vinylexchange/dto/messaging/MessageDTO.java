package com.atamanahmet.vinylexchange.dto.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Message;
import com.atamanahmet.vinylexchange.domain.enums.MessageType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private long id;
    private UUID conversationId;
    private UUID senderId;
    private String senderUsername;
    private String receiverUsername;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public MessageDTO from(Message message) {

        return MessageDTO.builder()
                .conversationId(message.getConversationId())
                .id(message.getId())
                .senderUsername(message.getSenderUsername())
                .receiverUsername(message.getReceiverUsername())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .isRead(message.isRead())
                .timestamp(message.getTimestamp())
                .build();
    }
}
