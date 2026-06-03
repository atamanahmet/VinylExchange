package com.atamanahmet.vinylexchange.domain;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.NotificationType;

public record NotificationCommand(
        NotificationType type,
        String title,
        String message,
        UUID relatedListingId) {

}
