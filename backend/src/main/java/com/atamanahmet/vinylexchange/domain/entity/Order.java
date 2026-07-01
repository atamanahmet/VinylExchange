package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;
import com.atamanahmet.vinylexchange.security.encryption.PiiAttributeConverter;

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
    // SHIPMENT FIELDS
    //

    /** Basit Kargo order ID returned on shipment creation */
    @Column(name = "shipment_order_id")
    private String shipmentOrderId;

    /** Barcode seller prints and takes to shipment branch */
    @Column(name = "shipment_barcode")
    private String shipmentBarcode;

    /** Shipment company code e.g. MNG, ARAS, YURTICI */
    @Column(name = "shipment_handler_code")
    private String shipmentHandlerCode;

    /** Tracking number from the actual shipment company, set after pickup */
    @Column(name = "shipment_tracking_number")
    private String shipmentTrackingNumber;

    /** SVG label URL stored after generation */
    @Column(name = "shipment_label_url")
    private String shipmentLabelUrl;

    @Column(name = "shipment_label_generated_at")
    private LocalDateTime shipmentLabelGeneratedAt;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(name = "shipping_address_snapshot", columnDefinition = "TEXT")
    private String shippingAddressSnapshot;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(name = "billing_address_snapshot", columnDefinition = "TEXT")
    private String billingAddressSnapshot;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(name = "seller_address_snapshot", columnDefinition = "TEXT")
    private String sellerAddressSnapshot;

    //
    // PAYOUT FIELDS
    //

    /** True when order completes and payout is waiting to be processed */
    @Builder.Default
    @Column(name = "payout_pending", nullable = false)
    private boolean payoutPending = false;

    /** Set by admin when payout is manually processed */
    @Column(name = "payout_sent_at")
    private LocalDateTime payoutSentAt;

    /** Admin userId who processed the payout */
    @Column(name = "payout_sent_by")
    private String payoutSentBy;
}