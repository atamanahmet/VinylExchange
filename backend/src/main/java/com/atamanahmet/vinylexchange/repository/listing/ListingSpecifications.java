package com.atamanahmet.vinylexchange.repository.listing;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.atamanahmet.vinylexchange.domain.entity.Genre;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import com.atamanahmet.vinylexchange.domain.entity.User;

public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<Listing> isPubliclyAvailable() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), ListingStatus.AVAILABLE),
                cb.greaterThan(root.get("stockQuantity"), 0),
                cb.isFalse(root.get("onHold")));
    }

    public static Specification<Listing> hasCountry(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("country").in(countries);
    }

    public static Specification<Listing> hasFormat(List<MediaFormat> formats) {
        if (formats == null || formats.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("mediaInfo").get("format").in(formats);
    }

    public static Specification<Listing> hasSpeedRpm(List<Integer> speedRpm) {
        if (speedRpm == null || speedRpm.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("mediaInfo").get("speedRpm").in(speedRpm);
    }

    public static Specification<Listing> hasVinylSubtype(List<VinylSubtype> vinylSubtypes) {
        if (vinylSubtypes == null || vinylSubtypes.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("mediaInfo").get("vinylSubtype").in(vinylSubtypes);
    }

    public static Specification<Listing> hasCondition(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("condition").in(conditions);
    }

    public static Specification<Listing> hasYearFrom(Integer yearFrom) {
        if (yearFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("year"), yearFrom);
    }

    public static Specification<Listing> hasYearTo(Integer yearTo) {
        if (yearTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("year"), yearTo);
    }

    public static Specification<Listing> hasPriceFrom(Long priceFromKurus) {
        if (priceFromKurus == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceKurus"), priceFromKurus);
    }

    public static Specification<Listing> hasPriceTo(Long priceToKurus) {
        if (priceToKurus == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceKurus"), priceToKurus);
    }

    public static Specification<Listing> hasGenreIds(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Listing> subRoot = subquery.from(Listing.class);
            Join<Listing, Genre> genreJoin = subRoot.join("genres");
            subquery.select(cb.literal(1));
            subquery.where(
                    cb.equal(subRoot.get("id"), root.get("id")),
                    genreJoin.get("id").in(genreIds));
            return cb.exists(subquery);
        };
    }

    public static Specification<Listing> hasTradeable(Boolean tradeable) {
        if (tradeable == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tradeable"), tradeable);
    }

    public static Specification<Listing> hasOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            Join<Listing, User> owner = root.join("owner");
            return cb.equal(owner.get("username"), ownerUsername);
        };
    }
}
