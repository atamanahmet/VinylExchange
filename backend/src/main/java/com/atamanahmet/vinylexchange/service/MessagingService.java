package com.atamanahmet.vinylexchange.service;



import java.util.List;

import java.util.Map;

import java.util.UUID;

import java.util.stream.Collectors;



import com.atamanahmet.vinylexchange.domain.entity.Conversation;

import com.atamanahmet.vinylexchange.domain.entity.Message;

import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;

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

import com.atamanahmet.vinylexchange.dto.messaging.SendMessageResponse;

import com.atamanahmet.vinylexchange.domain.enums.MessageType;



import com.atamanahmet.vinylexchange.exception.ConversationNotFoundException;

import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;

import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;



@Service

public class MessagingService {



    private final MessagingRepository messagingRepository;

    private final ConversationRepository conversationRepository;

    private final ListingService listingService;

    private final ListingRepository listingRepository;



    private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);



    public MessagingService(

            MessagingRepository messagingRepository,

            ConversationRepository conversationRepository,

            ListingService listingService,

            ListingRepository listingRepository) {



        this.messagingRepository = messagingRepository;

        this.conversationRepository = conversationRepository;

        this.listingService = listingService;

        this.listingRepository = listingRepository;



        logger.info("Messaging service ready.");

    }



    @Transactional

    public ConversationDTO startConversation(

            UUID initiatorId,

            String initiatorUsername,

            String listingPublicId) {



        Listing listing = listingService.findListingByPublicId(listingPublicId);

        return startConversation(initiatorId, initiatorUsername, listing.getId());

    }



    @Transactional

    public ConversationDTO startConversation(

            UUID initiatorId,

            String initiatorUsername,

            UUID relatedListingId) {



        Listing listing = listingService.findListingById(relatedListingId);



        if (!listing.isAvailable()) {

            throw new ListingNotFoundException("Listing is not available for trade, cannot start conversation");

        }



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



                    } catch (DataIntegrityViolationException e) {



                        return conversationRepository.findBetweenUsers(initiatorId, participantId, relatedListingId)

                                .orElseThrow(

                                        () -> new RuntimeException("Conversation creation issues: MessagingService"));

                    }

                });



        return convertToDTO(conversation, listing.getPublicId());

    }



    private Conversation getConversation(UUID conversationId, UUID userId) {



        Conversation conversation = conversationRepository.findById(conversationId)

                .orElseThrow(() -> new ConversationNotFoundException());



        if (!conversation.isUserParticipant(userId)) {

            throw new UnauthorizedActionException("User in not part of this conversation");

        }

        return conversation;

    }



    private Conversation getConversation(String publicId, UUID userId) {



        Conversation conversation = conversationRepository.findByPublicId(publicId)

                .orElseThrow(ConversationNotFoundException::new);



        if (!conversation.isUserParticipant(userId)) {

            throw new UnauthorizedActionException("User in not part of this conversation");

        }

        return conversation;

    }



    public ConversationDTO getConversationDTO(String publicId, UUID userId) {



        Conversation conversation = getConversation(publicId, userId);



        conversation.resetUnreadCount(userId);



        conversationRepository.save(conversation);



        return convertToDTO(conversation);

    }



    @Transactional

    public SendMessageResponse sendMessage(

            UUID senderId,

            String senderUsername,

            String conversationPublicId,

            String listingPublicId,

            String content,

            MessageType messageType) {



        Listing listing = listingService.findListingByPublicId(listingPublicId);

        return sendMessage(senderId, senderUsername, conversationPublicId, listing.getId(), content, messageType);

    }



    @Transactional

    public SendMessageResponse sendMessage(

            UUID senderId,

            String senderUsername,

            String conversationPublicId,

            UUID relatedListingId,

            String content,

            MessageType messageType) {



        listingService.findListingById(relatedListingId);



        Conversation conversation;

        ConversationDTO conversationDTO;



        if (conversationPublicId == null || conversationPublicId.isBlank()) {

            conversationDTO = startConversation(senderId, senderUsername, relatedListingId);

            conversation = conversationRepository.findByPublicId(conversationDTO.getPublicId())

                    .orElseThrow(ConversationNotFoundException::new);

        } else {

            conversation = getConversation(conversationPublicId, senderId);

            conversationDTO = convertToDTO(conversation);

        }



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



        MessageDTO messageDTO = new MessageDTO().from(savedMessage);

        return new SendMessageResponse(conversationDTO, messageDTO);

    }



    public Page<MessageDTO> getMessages(UUID userId, String conversationPublicId, int page, int size) {



        Conversation conversation = getConversation(conversationPublicId, userId);



        Pageable pageable = PageRequest.of(page, size);



        Page<Message> messagePage = messagingRepository.findByConversationIdOrderByTimestampAsc(conversation.getId(),

                pageable);



        return messagePage.map(message -> new MessageDTO().from(message));

    }



    private ConversationDTO convertToDTO(Conversation conversation) {

        String listingPublicId = listingRepository.findPublicIdById(conversation.getRelatedListingId())

                .orElse(null);

        return convertToDTO(conversation, listingPublicId);

    }



    private ConversationDTO convertToDTO(Conversation conversation, String listingPublicId) {



        String lastMessagePreview = messagingRepository.findLatestMessageByConversationId(conversation.getId())

                .map(message -> message.getContent().length() > 50 ? message.getContent().substring(0, 50)

                        : message.getContent())

                .orElse("No messages yet");



        return new ConversationDTO().from(

                conversation,

                "",

                lastMessagePreview,

                listingPublicId);

    }



    @Transactional(readOnly = true)

    public List<ConversationDTO> getUserConversations(UUID userId) {



        List<Conversation> conversationList = conversationRepository.findAllByUserId(userId);



        List<UUID> listingIds = conversationList.stream()

                .map(Conversation::getRelatedListingId)

                .distinct()

                .toList();



        Map<UUID, String> listingPublicIds = listingRepository.findAllByIdIn(listingIds).stream()

                .collect(Collectors.toMap(Listing::getId, Listing::getPublicId));



        return conversationList.stream()

                .map(conv -> convertToDTO(conv, listingPublicIds.get(conv.getRelatedListingId())))

                .collect(Collectors.toList());

    }



    public Long getUserTotalUnreadCount(UUID userId) {

        return conversationRepository.getTotalUnreadCountForUser(userId);

    }



    public void deleteMyConversations(UUID userId) {



        List<UUID> conversations = conversationRepository.findAllByUserId(userId).stream().map(conv -> conv.getId())

                .collect(Collectors.toList());



        conversationRepository.deleteAllById(conversations);

    }



    public void deleteThisConversation(String conversationPublicId, UUID userId) {



        Conversation conversation = getConversation(conversationPublicId, userId);



        conversationRepository.delete(conversation);

    }

}


