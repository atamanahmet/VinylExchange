package com.atamanahmet.vinylexchange.dto.messaging;



import java.time.LocalDateTime;



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



    private String publicId;



    private String initiatorUsername;



    private String listingPublicId;



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

            String lastMessagePreview,

            String listingPublicId) {



        return ConversationDTO.builder()

                .publicId(conversation.getPublicId())

                .initiatorUsername(conversation.getInitiatorUsername())

                .participantUsername(conversation.getParticipantUsername())

                .participantAvatar(participantAvatar)

                .listingPublicId(listingPublicId)

                .lastMessagePreview(lastMessagePreview)

                .createdAt(conversation.getCreatedAt())

                .lastMessageAt(conversation.getLastMessageAt())

                .build();

    }

}


