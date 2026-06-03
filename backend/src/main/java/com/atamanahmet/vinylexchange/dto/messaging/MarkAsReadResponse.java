package com.atamanahmet.vinylexchange.dto.messaging;

import java.util.UUID;

public record MarkAsReadResponse(
        UUID notificationId,
        int unreadCount) {
}