package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "listings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Listing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String packaging;
    private int year;
    private String country;
    private String barcode;
    private String format;
    private String description;
    private String artistName;
    private String artistId;
    private String labelName;
    private String condition;
    private UUID mbId;

    @Builder.Default
    private int stockQuantity = 5;

    @Builder.Default
    private boolean onHold = false;

    private Integer trackCount;
    private Boolean tradeable;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.AVAILABLE;

    /** What buyer pays, stored in kurus */
    @Column(name = "price_kurus", nullable = false)
    private long priceKurus;

    /** What seller receives after platform cut, stored in kurus */
    @Column(name = "seller_earnings_kurus", nullable = false)
    private long sellerEarningsKurus;

    /** Platform cut in kurus, platformCutKurus + sellerEarningsKurus = priceKurus always */
    @Column(name = "platform_cut_kurus", nullable = false)
    private long platformCutKurus;

    /** Platform fee in basis points locked at listing creation time */
    @Column(name = "platform_fee_bp", nullable = false)
    private int platformFeeBP;

    /**
     * Price at listing creation, never updated
     * Used with priceLastChangedAt to calculate buyer-visible discount
     */
    @Column(name = "original_price_kurus", nullable = false)
    private long originalPriceKurus;

    /**
     * Set when seller updates the price, null if price never changed
     * Discount only shown to buyer after 30 days from this date
     */
    @Column(name = "price_last_changed_at")
    private LocalDateTime priceLastChangedAt;

    /** Basit Kargo handler code, null means platform picks ECONOMIC */
    @Column(name = "preferred_cargo_company")
    private String preferredCargoCompany;

    @Column(name = "trade_value")
    private long tradeValue;

    @Builder.Default
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradePreference> tradePreferences = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleType saleType = SaleType.FIXED_PRICE;

    @Builder.Default
    @Column(nullable = false)
    private boolean promote = false;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<ListingImage> images = new ArrayList<>();

    private UUID promotedById;
    private String promotedBy;
    private LocalDateTime promotedAt;

    public UUID getOwnerId() {
        return this.owner.getId();
    }

    public boolean hasEnoughStock(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }

    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE && stockQuantity > 0;
    }

    public String getOwnerUsername() {
        return this.owner.getUsername();
    }

    public void addTradePreference(TradePreference newTradePreference) {
        newTradePreference.setListing(this);
        tradePreferences.add(newTradePreference);
    }

    public void removeTradePreference(TradePreference tradePreference) {
        tradePreferences.remove(tradePreference);
        tradePreference.setListing(null);
    }

}