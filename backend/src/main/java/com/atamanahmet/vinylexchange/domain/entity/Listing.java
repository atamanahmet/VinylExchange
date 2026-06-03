package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.atamanahmet.vinylexchange.config.json.DiscountDeserializer;
import com.atamanahmet.vinylexchange.config.json.DiscountSerializer;
import com.atamanahmet.vinylexchange.config.json.PriceKurusDeserializer;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;

import com.atamanahmet.vinylexchange.common.money.MoneyCalculator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("price")
    @JsonDeserialize(using = PriceKurusDeserializer.class)
    @JsonSerialize(using = PriceTlSerializer.class)
    @Column(name = "price_kurus")
    private long priceKurus; // samllest unit, cent/kurus

    @JsonDeserialize(using = PriceKurusDeserializer.class)
    @JsonSerialize(using = PriceTlSerializer.class)
    @Column(name = "trade_value")
    private long tradeValue;

    @Min(0)
    @Max(10_000)
    @JsonProperty("discount")
    @JsonDeserialize(using = DiscountDeserializer.class)
    @JsonSerialize(using = DiscountSerializer.class)
    @Column(name = "discount_bp")
    @Builder.Default
    private int discountBP = 0; // as basisPoint, /10_000

    @Transient
    @JsonProperty("discountedPrice")
    @JsonSerialize(using = PriceTlSerializer.class)
    public long getDiscountedPriceKurus() {
        return MoneyCalculator.discounted(priceKurus, discountBP);
    }

    @Builder.Default
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradePreference> tradePreferences = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @Builder.Default
    @Column(nullable = false)
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean promote = false;

    // helpers
    public UUID getOwnerId() {
        return this.owner.getId();
    }

    @Transient
    @JsonProperty("ownerUsername")
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
    //

    public boolean hasEnoughStock(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }

    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE && stockQuantity > 0;
    }

    // hasRole.ADMIN
    private UUID promotedById;
    private String promotedBy;
    private LocalDateTime promotedAt;

}