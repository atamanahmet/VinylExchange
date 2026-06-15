package com.atamanahmet.vinylexchange.controller;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.service.MessagingService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.atamanahmet.vinylexchange.dto.messaging.ConversationDTO;
import com.atamanahmet.vinylexchange.dto.messaging.MessageDTO;
import com.atamanahmet.vinylexchange.dto.messaging.MessagePageResponse;
import com.atamanahmet.vinylexchange.dto.messaging.SendMessageRequest;
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

                MessageDTO messageDTO = messagingService.sendMessage(
                        UserUtil.getCurrentUserId(),
                                UserUtil.getCurrentUserUsername(),
                                request.getConversationId(),
                                request.getRelatedListingId(),
                                request.getContent(), null);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(messageDTO);
        }

        @PostMapping("/start")
        public ResponseEntity<?> startConversation(
                        @RequestBody StartConversationRequest request) {

                // only when starting convo
                ConversationDTO conversationDTO = messagingService.startConversation(
                                UserUtil.getCurrentUserId(),
                                UserUtil.getCurrentUserUsername(),
                                request.relatedListingId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(conversationDTO);
        }

        @GetMapping("/conversation/{converstaionId}")
        public ResponseEntity<?> getMessagesByConversationId(
                        @PathVariable(name = "converstaionId") UUID converstaionId
        // @RequestParam(defaultValue = "0") int page,
        // @RequestParam(defaultValue = "50") int size
        ) {
                // checking if user participant with userId
                ConversationDTO conversationDTO = messagingService.getConversationDTO(converstaionId,
                                UserUtil.getCurrentUserId());

                Page<MessageDTO> messagePage = messagingService.getMessages(UserUtil.getCurrentUserId(), converstaionId, 0,
                                50);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(new MessagePageResponse(
                                                conversationDTO,
                                                messagePage));
        }

        @GetMapping("/conversations")
        public ResponseEntity<?> getConversations() {

                List<ConversationDTO> conversationsDTO = messagingService.getUserConversations(UserUtil.getCurrentUserId());

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
                        @PathVariable(name = "conversationId", required = true) UUID conversationId) {

                messagingService.deleteThisConversation(UserUtil.getCurrentUserId(), conversationId);

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
