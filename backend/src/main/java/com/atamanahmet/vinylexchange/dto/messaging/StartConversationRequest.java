package com.atamanahmet.vinylexchange.dto.messaging;

import jakarta.validation.constraints.NotBlank;

public record StartConversationRequest(@NotBlank String publicId) {

}
