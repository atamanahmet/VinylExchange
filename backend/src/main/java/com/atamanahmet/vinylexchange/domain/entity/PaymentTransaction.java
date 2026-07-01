package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sellerId;

    /**
     * One order = one escrow transaction
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * Amount held in escrow, in kurus (same unit as order.totalPrice)
     */
    @Column(nullable = false)
    private Long amountKurus;

    /**
     * Provider-generated payment reference, mock ID or real iyzico paymentId
     */
    @Column(name = "provider_checkout_token")
    private String providerCheckoutToken;

    /**
     * Provider-generated payout reference, set when payout is approved
     */
    @Column(name = "provider_payout_id")
    private String providerPayoutId;

    /** Iyzico internal payment ID from verify response */
    @Column(name = "provider_internal_payment_id")
    private String providerInternalPaymentId;

    /** Auth code from bank */
    @Column(name = "auth_code")
    private String authCode;

    /** Host reference from bank */
    @Column(name = "host_reference")
    private String hostReference;

    /** Fraud status: 1=passed, -1=failed, 0=review */
    @Column(name = "fraud_status")
    private Integer fraudStatus;

    /**
     * When payment captured by provider
     */
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;


    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    /**
     * Buyer confirm or auto-confirm
     */
    @Column(name = "payout_approved_at")
    private LocalDateTime payoutApprovedAt;


    @Column(name = "auto_confirm_deadline")
    private LocalDateTime autoConfirmDeadline;

    @Column(name = "dispute_reason", length = 500)
    private String disputeReason;

    /**
     * Admin or system note when dispute is resolved
     */
    @Column(name = "dispute_resolution_note", length = 500)
    private String disputeResolutionNote;

    /**
     * Set when a verified provider callback arrives after payment already reached
     * a terminal or in-flight payout state. Ops follow-up required; no auto-refund.
     */
    @Column(name = "refund_review_required", nullable = false)
    @Builder.Default
    private boolean refundReviewRequired = false;
}