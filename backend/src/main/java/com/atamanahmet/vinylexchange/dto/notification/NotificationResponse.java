package com.atamanahmet.vinylexchange.dto.notification;

import java.util.List;

public record NotificationResponse(
        List<NotificationDTO> notifications,
        int unreadCount
) {}
