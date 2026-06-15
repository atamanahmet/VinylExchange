package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ListingDTO {

    private UUID id;
    private UUID mbId;
    private String title;
    private String description;
    private String format;
    private String condition;
    private String packaging;
    private String barcode;
    private String artistName;
    private String labelName;
    private String ownerUsername;
    private String country;
    private int year;
    private int stockQuantity;
    private Boolean tradeable;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private List<String> imagePaths;
    private List<TradePreferenceDTO> tradePreferences;
    private Integer trackCount;

    @JsonProperty("price")
    @JsonSerialize(using = PriceTlSerializer.class)
    private long priceKurus;

    @JsonSerialize(using = PriceTlSerializer.class)
    private long tradeValue;

    /**
     * Only populated on detail page when a qualifying discount exists
     * Null on list/card view and when no 30-day discount qualifies
     */
    @JsonSerialize(using = PriceTlSerializer.class)
    private Long originalPriceKurus;

    /**
     * Percentage drop from original price, null if no qualifying discount
     * Calculated by ListingPriceCalculator, never stored
     */
    private Integer discountPercent;

    public ListingDTO(Listing listing, List<String> imagePaths, Integer discountPercent) {
        this.id = listing.getId();
        this.mbId = listing.getMbId();
        this.title = listing.getTitle();
        this.description = listing.getDescription();
        this.year = listing.getYear();
        this.priceKurus = listing.getPriceKurus();
        this.tradeable = listing.getTradeable();
        this.imagePaths = imagePaths;
        this.format = listing.getFormat();
        this.country = listing.getCountry();
        this.status = listing.getStatus();
        this.packaging = listing.getPackaging();
        this.ownerUsername = listing.getOwner().getUsername();
        this.trackCount = listing.getTrackCount();
        this.stockQuantity = listing.getStockQuantity();
        this.tradePreferences = TradePreferenceDTO.fromEntities(listing.getTradePreferences());
        this.barcode = listing.getBarcode();
        this.artistName = listing.getArtistName();
        this.condition = listing.getCondition();
        this.labelName = listing.getLabelName();
        this.createdAt = listing.getCreatedAt();
        this.tradeValue = listing.getTradeValue();
        this.discountPercent = discountPercent;
        this.originalPriceKurus = discountPercent != null ? listing.getOriginalPriceKurus() : null;
    }
}