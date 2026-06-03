package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;
import com.atamanahmet.vinylexchange.dto.listing.CreateListingRequest;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.listing.UpdateListingRequest;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceDTO;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceRequest;
import com.atamanahmet.vinylexchange.service.media.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListingMapper {

    private final FileStorageService fileStorageService;

    /**
     * Converts CreateListingRequest to Listing entity
     * Handles null trade preferences
     */
    public Listing toEntity(CreateListingRequest request) {

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .artistName(request.getArtistName())
                .description(request.getDescription())
                .priceKurus(request.getPriceKurus())
                .discountBP(request.getDiscountBP())
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
                .tradePreferences(new ArrayList<>())  // Initialize with empty list
                .build();

        // Only add trade preferences if they exist
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

    public void updateEntityFromRequest(Listing listing, UpdateListingRequest request) {
        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getArtistName() != null) {
            listing.setArtistName(request.getArtistName());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getPriceKurus() != null) {
            listing.setPriceKurus(request.getPriceKurus());
        }
        if (request.getDiscountBP() != null) {
            listing.setDiscountBP(request.getDiscountBP());
        }
        if (request.getTradeable() != null) {
            listing.setTradeable(request.getTradeable());
        }
        if (request.getTradeValue() != null) {
            listing.setTradeValue(request.getTradeValue());
        }
        if (request.getFormat() != null) {
            listing.setFormat(request.getFormat());
        }
        if (request.getCondition() != null) {
            listing.setCondition(request.getCondition());
        }
        if (request.getPackaging() != null) {
            listing.setPackaging(request.getPackaging());
        }
        if (request.getYear() != null) {
            listing.setYear(request.getYear());
        }
        if (request.getCountry() != null) {
            listing.setCountry(request.getCountry());
        }
        if (request.getBarcode() != null) {
            listing.setBarcode(request.getBarcode());
        }
        if (request.getLabelName() != null) {
            listing.setLabelName(request.getLabelName());
        }
        if (request.getTrackCount() != null) {
            listing.setTrackCount(request.getTrackCount());
        }
        if (request.getMbId() != null) {
            listing.setMbId(request.getMbId());
        }
        if (request.getStockQuantity() !=listing.getStockQuantity()) {
            listing.setStockQuantity(request.getStockQuantity());
        }

        // Update trade preferences
        if (request.getTradePreferences() != null) {
            listing.getTradePreferences().clear();
            request.getTradePreferences().forEach(prefRequest -> {
                listing.addTradePreference(toTradePreferenceEntity(prefRequest));
            });
        }
    }

    /**
     * Convert TradePreferenceRequest to TradePreference entity
     */
    private TradePreference toTradePreferenceEntity(TradePreferenceRequest request) {
        TradePreference pref = new TradePreference();
        pref.setDesiredItem(request.getDesiredItem());
        pref.setExtraAmount(request.getExtraAmount());
        pref.setPaymentDirection(request.getPaymentDirection());
        return pref;
    }

    /**
     * Convert TradePreference entity to TradePreferenceDTO
     */
    private TradePreferenceDTO toTradePreferenceDTO(TradePreference entity) {
        return new TradePreferenceDTO(
                entity.getId(),
                entity.getDesiredItem(),
                entity.getExtraAmount(),
                entity.getPaymentDirection()
        );
    }

    /**
     * Convert Listing entity to ListingDTO
     */
    public ListingDTO toDTO(Listing listing, List<String> imagePaths) {

        return new ListingDTO(listing, imagePaths);
    }

}
