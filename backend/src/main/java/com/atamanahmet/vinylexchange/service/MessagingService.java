package com.atamanahmet.vinylexchange.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.domain.entity.Conversation;
import com.atamanahmet.vinylexchange.domain.entity.Message;
import com.atamanahmet.vinylexchange.repository.messaging.ConversationRepository;
import com.atamanahmet.vinylexchange.repository.messaging.MessagingRepository;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.Listing;

import com.atamanahmet.vinylexchange.dto.messaging.ConversationDTO;
import com.atamanahmet.vinylexchange.dto.messaging.MessageDTO;
import com.atamanahmet.vinylexchange.dto.messaging.ParticipantInfo;
import com.atamanahmet.vinylexchange.domain.enums.MessageType;

//exceptions
import com.atamanahmet.vinylexchange.exception.ConversationNotFoundException;
import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;

@Service
public class MessagingService {

    private final MessagingRepository messagingRepository;
    private final ConversationRepository conversationRepository;
    private final ListingService listingService;

    private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);

    public MessagingService(
            MessagingRepository messagingRepository,
            ConversationRepository conversationRepository,
            ListingService listingService) {

        this.messagingRepository = messagingRepository;
        this.conversationRepository = conversationRepository;
        this.listingService = listingService;

        logger.info("Messaging service ready.");
    }

    // start with only buyer to owner, no need to owner to buyer wOut convo
    @Transactional
    public ConversationDTO startConversation(
            UUID initiatorId,
            String initiatorUsername,
            UUID relatedListingId) {

        Listing listing = listingService.findListingById(relatedListingId);

        // to check availability, throws exception if not exist or available
        if (!listing.isAvailable()) {
            throw new ListingNotFoundException("Listing is not available for trade, cannot start conversation");
        }

        // do only when starting convo
        UUID participantId = listing.getOwnerId();

        if (initiatorId.equals(participantId)) {
            throw new UnauthorizedActionException("Conversation can not be start with yourself.");
        }

        String participantUsername = listing.getOwnerUsername();

        Conversation conversation = conversationRepository
                .findBetweenUsers(initiatorId, participantId, relatedListingId)
                .orElseGet(() -> {

                    try {
                        Conversation newConversation = new Conversation(initiatorId, initiatorUsername, participantId,
                                participantUsername, relatedListingId);

                        return conversationRepository.save(newConversation);

                        // if conversation is exist,
                        // but got existing id error due to spamming, try again
                    } catch (DataIntegrityViolationException e) {

                        return conversationRepository.findBetweenUsers(initiatorId, participantId, relatedListingId)
                                .orElseThrow(
                                        () -> new RuntimeException("Conversation creation issues: MessagingService"));
                    }
                });

        return convertToDTO(conversation);
    }

    private Conversation getConversation(UUID conversationId, UUID userId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException());

        if (!conversation.isUserParticipant(userId)) {
            throw new UnauthorizedActionException("User in not part of this conversation");
        }
        return conversation;
    }

    public ConversationDTO getConversationDTO(UUID conversationId, UUID userId) {

        Conversation conversation = getConversation(conversationId, userId);

        conversation.resetUnreadCount(userId);

        conversationRepository.save(conversation);

        return convertToDTO(conversation);
    }

    @Transactional
    public MessageDTO sendMessage(
            UUID senderId,
            String senderUsername,
            UUID conversationId,
            UUID relatedListingId,
            String content,
            MessageType messageType) {

        // throws listing not found ex
        listingService.findListingById(relatedListingId);

        Conversation conversation = getConversation(conversationId, senderId);

        ParticipantInfo participantInfo = conversation.getOtherParticipantInfo(senderId);

        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .senderUsername(senderUsername)
                .receiverUsername(participantInfo.username())
                .receiverId(participantInfo.id())
                .content(content)
                .messageType(messageType == null ? MessageType.TEXT : messageType)
                .build();

        Message savedMessage = messagingRepository.save(message);

        conversation.increaseUnreadCount(participantInfo.id());
        conversation.updateLastMessageTime();

        conversationRepository.save(conversation);

        return new MessageDTO().from(savedMessage);
    }

    public Page<MessageDTO> getMessages(UUID userId, UUID conversationId, int page,
            int size) {

        System.out.println("getting messages...");

        Conversation conversation = getConversation(conversationId, userId);

        System.out.println("convo loaded...");

        System.out.println("conversation id " + conversation.getId());
        System.out.println("initiator username " + conversation.getInitiatorUsername());
        System.out.println("reciever username " + conversation.getParticipantUsername());

        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messagePage = messagingRepository.findByConversationIdOrderByTimestampAsc(conversation.getId(),
                pageable);

        // messagePage.iterator().forEachRemaining(message ->
        // System.out.println(message.getContent()));

        Page<MessageDTO> messageDTOPage = messagePage.map(message -> new MessageDTO().from(message));

        return messageDTOPage;
    }

    private ConversationDTO convertToDTO(Conversation conversation) {

        String lastMessagePreview = messagingRepository.findLatestMessageByConversationId(conversation.getId())
                .map(message -> message.getContent().length() > 50 ? message.getContent().substring(0, 50)
                        : message.getContent())
                .orElse("No messages yet");

        return new ConversationDTO().from(
                conversation,
                "",
                lastMessagePreview);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(UUID userId) {

        List<Conversation> conversationList = conversationRepository.findAllByUserId(userId);

        List<ConversationDTO> conversationDTOList = conversationList.stream()
                .map((conv) -> convertToDTO(conv))
                .collect(Collectors.toList());

        return conversationDTOList;
    }

    public Long getUserTotalUnreadCount(UUID userId) {
        return conversationRepository.getTotalUnreadCountForUser(userId);
    }

    // TODO: only delete from deleting party, participant should have a copy
    // conversation history, separate entity immutable
    public void deleteMyConversations(UUID userId) {

        List<UUID> conversations = conversationRepository.findAllByUserId(userId).stream().map(conv -> conv.getId())
                .collect(Collectors.toList());

        conversationRepository.deleteAllById(conversations);
    }

    public void deleteThisConversation(UUID conversationId, UUID userId) {

        // checks if user participant, redundant
        Conversation conversation = getConversation(conversationId, userId);

        conversationRepository.delete(conversation);
    }
}
