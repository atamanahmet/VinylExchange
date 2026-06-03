package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.CancelRequestStatus;
import com.atamanahmet.vinylexchange.domain.enums.OrderItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cancel_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    private CancelRequestStatus status;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus previousOrderStatus;

    @Column(length = 500)
    private String reason;

    private LocalDateTime requestedAt;

    private LocalDateTime reviewedAt;

    private UUID reviewedBy;

    @Column(length = 500)
    private String reviewNote;
}
