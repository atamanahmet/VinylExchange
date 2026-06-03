package com.atamanahmet.vinylexchange.dto.messaging;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.MessageType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SendMessageRequest {

    private UUID conversationId;

    // @NotNull(message = "Receiver id is required")
    private UUID receiverId;

    @NotNull(message = "Related Listing id is required")
    private UUID relatedListingId;

    @Size(min = 2, max = 2000, message = "Message must be between 2 and 2000 characters")
    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
}
