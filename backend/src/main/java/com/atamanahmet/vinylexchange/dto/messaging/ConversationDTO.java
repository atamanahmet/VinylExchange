package com.atamanahmet.vinylexchange.dto.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.atamanahmet.vinylexchange.domain.entity.Conversation;

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
public class ConversationDTO {

    private UUID id;

    private UUID initiatorId;

    private String initiatorUsername;

    private UUID participantId;

    private UUID relatedListingId;

    private String participantUsername;

    private String participantAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageAt;

    private String lastMessagePreview;

    public ConversationDTO from(
            Conversation conversation,
            String participantAvatar,
            String lastMessagePreview) {

        return ConversationDTO.builder()
                .id(conversation.getId())
                .initiatorId(conversation.getInitiatorId())
                .initiatorUsername(conversation.getInitiatorUsername())
                .participantId(conversation.getParticipantId())
                .participantUsername(conversation.getParticipantUsername())
                .participantAvatar(participantAvatar)
                .relatedListingId(conversation.getRelatedListingId())
                .lastMessagePreview(lastMessagePreview)
                .createdAt(conversation.getCreatedAt())
                .lastMessageAt(conversation.getLastMessageAt())
                .build();
    }
}
