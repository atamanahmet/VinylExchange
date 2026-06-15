package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.common.money.ListingPriceCalculator;
import com.atamanahmet.vinylexchange.common.money.ListingPriceResult;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;
import com.atamanahmet.vinylexchange.dto.listing.CreateListingRequest;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.listing.UpdateListingRequest;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceDTO;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListingMapper {

    private final ListingPriceCalculator priceCalculator;

    /**
     * Converts CreateListingRequest to Listing entity
     * All three price fields and originalPriceKurus set from calculator
     */
    public Listing toEntity(CreateListingRequest request) {
        ListingPriceResult price = request.getSellerEarningsKurus() != null
                ? priceCalculator.fromSellerEarnings(request.getSellerEarningsKurus())
                : priceCalculator.fromBuyerPrice(request.getPriceKurus());

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .artistName(request.getArtistName())
                .description(request.getDescription())
                .priceKurus(price.priceKurus())
                .sellerEarningsKurus(price.sellerEarningsKurus())
                .platformCutKurus(price.platformCutKurus())
                .platformFeeBP(price.feeBP())
                .originalPriceKurus(price.priceKurus())
                .tradeable(request.getTradeable())
                .tradeValue(request.getTradeValue())
                .format(request.getFormat())
                .condition(request.getCondition())
                .packaging(request.getPackaging())
                .year(request.getYear())
                .country(request.getCountry())
                .barcode(request.getBarcode())
                .labelName(request.getLabelName())
                .artistId(request.getArtistId())
                .mbId(request.getMbId())
                .trackCount(request.getTrackCount())
                .stockQuantity(request.getStockQuantity())
                .tradePreferences(new ArrayList<>())
                .build();

        if (request.getTradePreferences() != null && !request.getTradePreferences().isEmpty()) {
            request.getTradePreferences().forEach(prefRequest -> {
                TradePreference pref = new TradePreference();
                pref.setDesiredItem(prefRequest.getDesiredItem());
                pref.setExtraAmount(prefRequest.getExtraAmount());
                pref.setPaymentDirection(prefRequest.getPaymentDirection());
                listing.addTradePreference(pref);
            });
        }

        return listing;
    }

    /**
     * Applies partial update from request onto existing entity
     * Price fields delegated to calculator when either price input is present
     */
    public void updateEntityFromRequest(Listing listing, UpdateListingRequest request) {
        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getArtistName() != null) listing.setArtistName(request.getArtistName());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getTradeable() != null) listing.setTradeable(request.getTradeable());
        if (request.getTradeValue() != null) listing.setTradeValue(request.getTradeValue());
        if (request.getFormat() != null) listing.setFormat(request.getFormat());
        if (request.getCondition() != null) listing.setCondition(request.getCondition());
        if (request.getPackaging() != null) listing.setPackaging(request.getPackaging());
        if (request.getYear() != null) listing.setYear(request.getYear());
        if (request.getCountry() != null) listing.setCountry(request.getCountry());
        if (request.getBarcode() != null) listing.setBarcode(request.getBarcode());
        if (request.getLabelName() != null) listing.setLabelName(request.getLabelName());
        if (request.getTrackCount() != null) listing.setTrackCount(request.getTrackCount());
        if (request.getMbId() != null) listing.setMbId(request.getMbId());
        if (request.getStockQuantity() != null) listing.setStockQuantity(request.getStockQuantity());

        if (request.getSellerEarningsKurus() != null || request.getPriceKurus() != null) {
            ListingPriceResult price = request.getSellerEarningsKurus() != null
                    ? priceCalculator.fromSellerEarnings(request.getSellerEarningsKurus())
                    : priceCalculator.fromBuyerPrice(request.getPriceKurus());
            listing.setPriceKurus(price.priceKurus());
            listing.setSellerEarningsKurus(price.sellerEarningsKurus());
            listing.setPlatformCutKurus(price.platformCutKurus());
            listing.setPlatformFeeBP(price.feeBP());
        }

        if (request.getTradePreferences() != null) {
            listing.getTradePreferences().clear();
            request.getTradePreferences().forEach(prefRequest ->
                    listing.addTradePreference(toTradePreferenceEntity(prefRequest)));
        }
    }

    /**
     * Card/list view, uses mainImageUrl only, no discount calculation
     */
    public ListingDTO toDTO(Listing listing) {
        List<String> imagePaths = listing.getMainImageUrl() != null
                ? List.of(listing.getMainImageUrl())
                : List.of();
        return new ListingDTO(listing, imagePaths, null);
    }

    /**
     * Detail view, full images, discount percent passed in from service
     */
    public ListingDTO toDTOWithImages(Listing listing, Integer discountPercent) {
        List<String> imagePaths = listing.getImages().stream()
                .map(ListingImage::getSecureUrl)
                .toList();
        return new ListingDTO(listing, imagePaths, discountPercent);
    }

    private TradePreference toTradePreferenceEntity(TradePreferenceRequest request) {
        TradePreference pref = new TradePreference();
        pref.setDesiredItem(request.getDesiredItem());
        pref.setExtraAmount(request.getExtraAmount());
        pref.setPaymentDirection(request.getPaymentDirection());
        return pref;
    }

    private TradePreferenceDTO toTradePreferenceDTO(TradePreference entity) {
        return new TradePreferenceDTO(
                entity.getId(),
                entity.getDesiredItem(),
                entity.getExtraAmount(),
                entity.getPaymentDirection()
        );
    }
}