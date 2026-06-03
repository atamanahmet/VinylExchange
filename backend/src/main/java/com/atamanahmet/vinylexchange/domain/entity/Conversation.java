package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.atamanahmet.vinylexchange.dto.messaging.ParticipantInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversations")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID initiatorId;

    @Column(nullable = false)
    private UUID participantId;

    @Column(nullable = false)
    private String initiatorUsername;

    @Column(nullable = false)
    private String participantUsername;

    @Column(nullable = false)
    private UUID relatedListingId;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private long initiatorUnreadCount = 0;

    @Column(nullable = false)
    private long participantUnreadCount = 0;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    public Conversation(UUID initiatorId, String initiatorUsername, UUID participantId, String participantUsername,
            UUID relatedListingId) {

        this.initiatorId = initiatorId;
        this.initiatorUsername = initiatorUsername;
        this.participantId = participantId;
        this.participantUsername = participantUsername;
        this.relatedListingId = relatedListingId;
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean isUserParticipant(UUID userId) {

        return this.initiatorId.equals(userId) || this.participantId.equals(userId);
    }

    public ParticipantInfo getOtherParticipantInfo(UUID userId) {

        // return this.initiatorId.equals(userId) ? this.participantId
        // : this.participantId.equals(userId) ? this.initiatorId : null;

        if (this.initiatorId.equals(userId)) {
            return new ParticipantInfo(this.participantId, this.participantUsername);
        } else if (this.participantId.equals(userId)) {
            return new ParticipantInfo(this.initiatorId, this.initiatorUsername);
        }

        throw new IllegalArgumentException("This User is not part of the conversation");
    }

    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void resetUnreadCount(UUID userId) {
        if (this.initiatorId.equals(userId)) {
            this.initiatorUnreadCount = 0;
        } else if (this.participantId.equals(userId)) {
            this.participantUnreadCount = 0;
        }
    }

    public void increaseUnreadCount(UUID userId) {
        if (this.initiatorId.equals(userId)) {
            this.initiatorUnreadCount++;
        } else if (this.participantId.equals(userId)) {
            this.participantUnreadCount++;
        }
    }

    public Long getUnreadCount(UUID userId) {
        if (this.initiatorId.equals(userId)) {
            return this.initiatorUnreadCount;
        } else if (this.participantId.equals(userId)) {
            return this.participantUnreadCount;
        }
        return null;
    }
}
