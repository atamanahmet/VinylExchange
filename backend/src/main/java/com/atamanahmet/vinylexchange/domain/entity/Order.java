package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;

import jakarta.persistence.*;
import lombok.*;

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

    /** Total buyer pays in kurus */
    @JsonSerialize(using = PriceTlSerializer.class)
    private Long totalPrice;

    /**
     * What seller receives after platform cut
     * Sum of sellerEarningsKurus across all order items
     */
    @JsonSerialize(using = PriceTlSerializer.class)
    @Column(name = "seller_earnings")
    private Long sellerEarnings;

    /**
     * Platform cut = totalPrice - sellerEarnings
     */
    @JsonSerialize(using = PriceTlSerializer.class)
    @Column(name = "platform_cut")
    private Long platformCut;

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

    //
    // CARGO FIELDS
    //

    /** Basit Kargo order ID returned on shipment creation */
    @Column(name = "cargo_order_id")
    private String cargoOrderId;

    /** Barcode seller prints and takes to cargo branch */
    @Column(name = "cargo_barcode")
    private String cargoBarcode;

    /** Cargo company code e.g. MNG, ARAS, YURTICI */
    @Column(name = "cargo_handler_code")
    private String cargoHandlerCode;

    /** Tracking number from the actual cargo company, set after pickup */
    @Column(name = "cargo_tracking_number")
    private String cargoTrackingNumber;

    /** SVG label URL stored after generation */
    @Column(name = "cargo_label_url")
    private String cargoLabelUrl;

    @Column(name = "cargo_label_generated_at")
    private LocalDateTime cargoLabelGeneratedAt;

    //
    // PAYOUT FIELDS
    //

    /** True when order completes and payout is waiting to be processed */
    @Builder.Default
    @Column(nullable = false)
    private boolean payoutPending = false;

    /** Set by admin when payout is manually processed */
    @Column(name = "payout_sent_at")
    private LocalDateTime payoutSentAt;

    /** Admin userId who processed the payout */
    @Column(name = "payout_sent_by")
    private String payoutSentBy;
}