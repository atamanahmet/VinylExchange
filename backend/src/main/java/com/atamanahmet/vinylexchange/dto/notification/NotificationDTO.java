package com.atamanahmet.vinylexchange.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record NotificationDTO(
        UUID id,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt,
        UUID relatedListingId
) {}
