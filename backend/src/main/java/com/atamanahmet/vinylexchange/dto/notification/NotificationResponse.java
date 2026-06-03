package com.atamanahmet.vinylexchange.dto.notification;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
    List<NotificationDTO> notifications;
    int unreadCount;
}
