package com.atamanahmet.vinylexchange.controller;

import java.util.UUID;

import com.atamanahmet.vinylexchange.service.NotificationService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.messaging.MarkAsReadResponse;
import com.atamanahmet.vinylexchange.dto.notification.NotificationResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
        private final NotificationService notificationService;

        @GetMapping
        public ResponseEntity<NotificationResponse> getNotifications(
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                NotificationResponse notifications = notificationService
                                .getAllNotificationsByUserId(UserUtil.getCurrentUserId(), pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(notifications);
        }

        @GetMapping("/dropdown")
        public ResponseEntity<NotificationResponse> getDropdownNotifications(
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                NotificationResponse notifications = notificationService
                                .getAllNotificationsByUserId(UserUtil.getCurrentUserId(), pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(notifications);
        }

        @GetMapping("/unread")
        public ResponseEntity<NotificationResponse> getUnread(
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                NotificationResponse notifications = notificationService.getUnreadNotifications(UserUtil.getCurrentUserId(),
                                pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(notifications);
        }

        @PostMapping("/{notificationId}/read")
        public ResponseEntity<MarkAsReadResponse> markAsRead(
                        @PathVariable(name = "notificationId") UUID notificationId) {

                System.out.println("read request: " + notificationId);

                notificationService.markAsRead(notificationId, UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .build();
        }
}
