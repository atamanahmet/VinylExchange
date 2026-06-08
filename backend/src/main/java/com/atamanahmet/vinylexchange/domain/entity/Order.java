package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Trackable user facing number shown to buyer/seller */
    @Column(name = "order_number", unique = true, nullable = false)
    private Long orderNumber;

    @Column(nullable = false)
    private UUID buyerId;

    /** One order = one seller */
    @Column(nullable = false)
    private UUID sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleType saleType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @JsonSerialize(using = PriceTlSerializer.class)
    private Long totalPrice;

    /** Payment window, scheduler cancels order after this */
    private LocalDateTime paymentExpiresAt;

    /** Seller must ship by this date */
    private LocalDateTime shippingDeadline;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime deliveredAt;

    /** Auto-confirm fires if buyer does not act before this deadline */
    private LocalDateTime autoConfirmDeadline;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}