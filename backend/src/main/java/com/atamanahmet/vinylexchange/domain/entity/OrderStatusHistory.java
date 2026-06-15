package com.atamanahmet.vinylexchange.domain.entity;

import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Append-only, never update or delete rows
 * Legal audit trail for order state changes
 */
@Entity
@Table(name = "order_status_history")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private OrderStatus toStatus;

    /**
     * userId or SYSTEM or SCHEDULER
     */
    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 500)
    private String note;
}