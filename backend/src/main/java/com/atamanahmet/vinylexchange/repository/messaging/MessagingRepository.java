package com.atamanahmet.vinylexchange.repository.messaging;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessagingRepository extends JpaRepository<Message, UUID> {

        Page<Message> findByConversationIdOrderByTimestampAsc(UUID conversationId, Pageable pageable);

        // for notificstion preview
        @Query("SELECT m from Message m " +
                        "WHERE m.conversationId = :conversationId " +
                        "ORDER BY m.timestamp DESC " +
                        "LIMIT 1")
        Optional<Message> findLatestMessageByConversationId(@Param("conversationId") UUID conversationId);

        // unreadCount for user
        @Query("SELECT COUNT(m) FROM Message m " +
                        "WHERE m.conversationId = :conversationId AND m.senderId != :userId " +
                        "AND m.isRead = false")
        long unreadCountByConversationIdAndUserId(
                        @Param("conversationId") UUID conversationId,
                        @Param("userId") UUID userId);

        // todo mark as all read

}
