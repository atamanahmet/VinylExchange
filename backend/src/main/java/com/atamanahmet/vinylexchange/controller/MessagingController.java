package com.atamanahmet.vinylexchange.controller;

import java.util.List;

import com.atamanahmet.vinylexchange.service.MessagingService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.atamanahmet.vinylexchange.dto.messaging.ConversationDTO;
import com.atamanahmet.vinylexchange.dto.messaging.MessageDTO;
import com.atamanahmet.vinylexchange.dto.messaging.MessagePageResponse;
import com.atamanahmet.vinylexchange.dto.messaging.SendMessageRequest;
import com.atamanahmet.vinylexchange.dto.messaging.SendMessageResponse;
import com.atamanahmet.vinylexchange.dto.messaging.StartConversationRequest;
import com.atamanahmet.vinylexchange.dto.messaging.UnreadCountResponse;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagingController {

        private final MessagingService messagingService;

        @PostMapping
        public ResponseEntity<?> sendMessage(
                        @RequestBody SendMessageRequest request) {

                SendMessageResponse response = messagingService.sendMessage(
                                UserUtil.getCurrentUserId(),
                                UserUtil.getCurrentUserUsername(),
                                request.getConversationPublicId(),
                                request.getPublicId(),
                                request.getContent(), request.getMessageType());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(response);
        }

        @PostMapping("/start")
        public ResponseEntity<?> startConversation(
                        @RequestBody StartConversationRequest request) {

                ConversationDTO conversationDTO = messagingService.startConversation(
                                UserUtil.getCurrentUserId(),
                                UserUtil.getCurrentUserUsername(),
                                request.publicId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(conversationDTO);
        }

        @GetMapping("/conversation/{conversationId}")
        public ResponseEntity<?> getMessagesByConversationId(
                        @PathVariable(name = "conversationId") String conversationId) {
                ConversationDTO conversationDTO = messagingService.getConversationDTO(conversationId,
                                UserUtil.getCurrentUserId());

                Page<MessageDTO> messagePage = messagingService.getMessages(
                                UserUtil.getCurrentUserId(),
                                conversationId,
                                0,
                                50);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(new MessagePageResponse(
                                                conversationDTO,
                                                messagePage));
        }

        @GetMapping("/conversations")
        public ResponseEntity<?> getConversations() {

                List<ConversationDTO> conversationsDTO = messagingService
                                .getUserConversations(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(conversationsDTO);
        }

        @DeleteMapping("/conversations")
        public ResponseEntity<?> deleteMyConversations() {

                messagingService.deleteMyConversations(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .build();
        }

        @DeleteMapping("/conversation/{conversationId}")
        public ResponseEntity<?> deleteThisConversation(
                        @PathVariable(name = "conversationId", required = true) String conversationId) {
                messagingService.deleteThisConversation(conversationId, UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .build();
        }

        @GetMapping("/unread")
        public ResponseEntity<UnreadCountResponse> getUnreadCount() {

                Long count = messagingService.getUserTotalUnreadCount(UserUtil.getCurrentUserId());
                return ResponseEntity.ok(new UnreadCountResponse(count));
        }
}
