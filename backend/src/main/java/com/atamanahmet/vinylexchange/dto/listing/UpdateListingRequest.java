package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.dto.user.TradePreferenceRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.atamanahmet.vinylexchange.config.json.DiscountDeserializer;
import com.atamanahmet.vinylexchange.config.json.PriceKurusDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateListingRequest {

    @Size(max = 200, message = "Title too long")
    private String title;

    @Size(max = 200, message = "Artist name too long")
    private String artistName;

    @Size(max = 500, message = "Description too long")
    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    @JsonProperty("price")
    @JsonDeserialize(using = PriceKurusDeserializer.class)
    private Long priceKurus;

    @Min(value = 0, message = "Seller earnings cannot be negative")
    @JsonProperty("sellerEarnings")
    @JsonDeserialize(using = PriceKurusDeserializer.class)
    private Long sellerEarningsKurus;

    @JsonProperty("discount")
    @JsonDeserialize(using = DiscountDeserializer.class)
    @Min(0)
    @Max(10_000)
    private Integer discountBP;

    private Boolean tradeable;

    @JsonDeserialize(using = PriceKurusDeserializer.class)
    private Long tradeValue;

    @Valid
    private List<TradePreferenceRequest> tradePreferences;

    private String format;

    private UUID mbId;

    private Integer stockQuantity;
    private String condition;
    private String packaging;
    private Integer year;
    private String country;
    private String barcode;
    private String labelName;
    private Integer trackCount;

    // For image handling
    private List<String> imagePaths;

    /**
     * Both null = no price change on this update (allowed)
     * Exactly one non-null = price change, backend calculates the other (allowed)
     * Both non-null = error
     */
    @AssertTrue(message = "Provide at most one of price or sellerEarnings, not both")
    @JsonIgnore
    public boolean isPriceInputValid() {
        return priceKurus == null || sellerEarningsKurus == null;
    }
}