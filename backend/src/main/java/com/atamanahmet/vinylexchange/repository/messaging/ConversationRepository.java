package com.atamanahmet.vinylexchange.repository.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

        @Query("Select c from Conversation c " +
                        "WHERE c.initiatorId= :userId OR c.participantId = :userId " +
                        "ORDER BY c.lastMessageAt DESC")
        List<Conversation> findAllByUserId(@Param("userId") UUID userId);

        @Query("Select c from Conversation c " +
                        "WHERE ((c.initiatorId= :userIdOne AND c.participantId = :userIdTwo) " +
                        "OR (c.initiatorId= :userIdTwo AND c.participantId = :userIdOne)) " +
                        "AND c.relatedListingId= :relatedListingId " +
                        "ORDER BY c.lastMessageAt DESC")
        Optional<Conversation> findBetweenUsers(
                        @Param("userIdOne") UUID userIdOne,
                        @Param("userIdTwo") UUID userIdTwo,
                        @Param("relatedListingId") UUID relatedListingId);

        Optional<Conversation> findByRelatedListingId(UUID listingId);

        @Query("SELECT COALESCE(SUM(CASE WHEN c.initiatorId = :userId THEN c.initiatorUnreadCount " +
                        "WHEN c.participantId = :userId THEN c.participantUnreadCount ELSE 0 END), 0) " +
                        "FROM Conversation c WHERE c.initiatorId = :userId OR c.participantId = :userId")
        Long getTotalUnreadCountForUser(@Param("userId") UUID userId);
}
