package com.atamanahmet.vinylexchange.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
    private UUID id;

    private String title;

    private String message;

    private boolean read;

    private LocalDateTime createdAt;

    private UUID relatedListingId;
}
