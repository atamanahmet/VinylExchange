package com.atamanahmet.vinylexchange.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Append-only price change log for a listing
 * Mirrors PaymentStatusHistory pattern
 */
@Entity
@Table(name = "listing_price_history")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListingPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    /**
     * userId string or "SYSTEM" for scheduler triggered changes
     * Stores UUID string only, no username, KVKK compliant
     */
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "old_price_kurus")
    private Long oldPriceKurus;

    @Column(name = "new_price_kurus", nullable = false)
    private long newPriceKurus;

    @Column(name = "old_seller_earnings_kurus")
    private Long oldSellerEarningsKurus;

    @Column(name = "new_seller_earnings_kurus", nullable = false)
    private long newSellerEarningsKurus;

    @Column(name = "old_platform_cut_kurus")
    private Long oldPlatformCutKurus;

    @Column(name = "new_platform_cut_kurus", nullable = false)
    private long newPlatformCutKurus;

    /**
     * Fee rate that was active at the time of this price entry
     */
    @Column(name = "fee_bp_at_change", nullable = false)
    private int feeBpAtChange;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 500)
    private String note;
}