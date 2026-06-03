package com.atamanahmet.vinylexchange.domain.enums;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Builder
@Getter
@Setter
public class UserStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // for audit, admin, moderator etc.
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column
    @Enumerated(EnumType.STRING)
    private UserStatus previousStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(nullable = false)
    private UUID changedById;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = true)
    private String metadata;

}
