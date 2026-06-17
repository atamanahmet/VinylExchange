package com.atamanahmet.vinylexchange.service;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Notification;
import com.atamanahmet.vinylexchange.domain.NotificationCommand;
import com.atamanahmet.vinylexchange.repository.NotificationRepository;
import com.atamanahmet.vinylexchange.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.notification.NotificationDTO;
import com.atamanahmet.vinylexchange.dto.notification.NotificationResponse;

import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    // private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {

        this.notificationRepository = notificationRepository;
        // this.userService = userService;
    }

    @Transactional
    public void notifyUsers(List<UUID> userIds, NotificationCommand command) {

        List<Notification> notifications = userIds.stream()
                .map(userId -> createNotification(userId, command))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public Notification createNotification(
            UUID userId,
            NotificationCommand command) {

        Notification notification = Notification.builder()
                .userId(userId)
                .title(command.title())
                .message(command.message())
                .type(command.type())
                .relatedListingId(command.relatedListingId())
                .build();

        return notificationRepository.save(notification);
    }

    public NotificationResponse getUnreadNotifications(UUID userId, Pageable pageable) {

        Page<Notification> page = notificationRepository.findByUserIdAndReadFalse(userId, pageable);

        List<NotificationDTO> notificationDTOs = convertToDTO(page.getContent());

        return new NotificationResponse(notificationDTOs, notificationDTOs.size());
    }

    public NotificationResponse getAllNotificationsByUserId(UUID userId, Pageable pageable) {

        Page<Notification> page = notificationRepository.findByUserId(userId, pageable);

        List<NotificationDTO> notificationDTOs = convertToDTO(page.getContent());

        int unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        return new NotificationResponse(notificationDTOs, unreadCount);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {

        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    public List<NotificationDTO> convertToDTO(List<Notification> notifications) {

        return notifications.stream().map(notification -> NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .relatedListingId(notification.getRelatedListingId())
                .build())
                .toList();
    }

    public void clearAll() {
        notificationRepository.deleteAll();
    }
}