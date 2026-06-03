package com.atamanahmet.vinylexchange.repository.user;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.TradePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.entity.Listing;

@Repository
public interface TradePreferenceRepository extends JpaRepository<TradePreference, UUID> {

    List<TradePreference> findByListing(Listing listing);

    List<TradePreference> findByListingId(UUID listingId);

    // for frontend showing listings that matches pref search
    @Query("SELECT DISTINCT tp.listing from TradePreference tp " +
            "WHERE LOWER(tp.desiredItem) " +
            "LIKE LOWER(CONCAT('%', :desiredItem ,'%'))")
    List<Listing> findListingsByDesiredItemContaining(@Param("desiredItem") String desiredItem);

    List<TradePreference> findByDesiredItemContainingIgnoreCase(String desiredItem);

    List<TradePreference> findByPaymentDirection(String paymentDirection);

    List<TradePreference> findByExtraAmountBetween(double minAmount, double maxAmount);

    void deleteByListingId(UUID listingId);
}
