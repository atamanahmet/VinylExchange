package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.domain.enums.Country;
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
public class CreateListingRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title too long")
    private String title;

    @NotBlank(message = "Artist name is required")
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
    private Integer discountBP = 0;

    // Trade-related
    private Boolean tradeable = false;

    @JsonDeserialize(using = PriceKurusDeserializer.class)
    private Long tradeValue = 0L;

    @Valid
    private List<TradePreferenceRequest> tradePreferences;

    // Album details
    @NotNull(message = "Media info is required")
    @Valid
    private MediaInfoDTO mediaInfo;

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotBlank(message = "Packaging is required")
    private String packaging;

    private Integer year;

    @NotNull(message = "Country is required")
    private Country country;
    private String barcode;
    private String labelName;
    private String artistId;
    private UUID mbId;

    @Min(value = 1, message = "Track count must be at least 1")
    private Integer trackCount = 1;

    @Min(value = 1, message = "Stock must be at least 1")
    private Integer stockQuantity = 1;

    /**
     * Exactly one of priceKurus or sellerEarningsKurus must be provided
     * Backend derives the other from the one given
     */
    @AssertTrue(message = "Provide exactly one of price or sellerEarnings, not both and not none")
    @JsonIgnore
    public boolean isPriceInputValid() {
        return (priceKurus != null) != (sellerEarningsKurus != null);
    }
}