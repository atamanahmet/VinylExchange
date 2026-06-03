package com.atamanahmet.vinylexchange.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.atamanahmet.vinylexchange.config.json.DiscountDeserializer;
import com.atamanahmet.vinylexchange.config.json.DiscountSerializer;
import com.atamanahmet.vinylexchange.config.json.PriceKurusDeserializer;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

    private UUID id;

    private UUID listingId;

    @JsonProperty("quantity")
    private int orderQuantity;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    private boolean committed;

    private String title;
    private String artistName;

    @JsonProperty("unitPrice")
    @JsonDeserialize(using = PriceKurusDeserializer.class)
    @JsonSerialize(using = PriceTlSerializer.class)
    private long pricePerUnit;

    @JsonProperty("totalPrice")
    @JsonDeserialize(using = PriceKurusDeserializer.class)
    @JsonSerialize(using = PriceTlSerializer.class)
    private long itemTotalPriceKurus;

    @Min(0)
    @Max(10_000)
    @JsonProperty("discountPerUnit")
    @JsonDeserialize(using = DiscountDeserializer.class)
    @JsonSerialize(using = DiscountSerializer.class)
    private int discountPerUnit; // as basisPoint, /10_000

    @JsonSerialize(using = PriceTlSerializer.class)
    private long discountedTotalPrice;

    private String mainImagePath;

}
