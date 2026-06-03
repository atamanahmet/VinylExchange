package com.atamanahmet.vinylexchange.dto.messaging;

import org.springframework.data.domain.Page;

public record MessagePageResponse(
        ConversationDTO conversationDTO,
        Page<MessageDTO> messagePage) {

}
