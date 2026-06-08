package com.atamanahmet.vinylexchange.repository.listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

        List<Listing> findAllByOwner_IdAndStatus(UUID ownerId, ListingStatus status);

        List<Listing> findAllByIdIn(List<UUID> listingIds);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT l FROM Listing l WHERE l.id IN :listingIds")
        List<Listing> findByIdInWithLock(List<UUID> listingIds);

        /**
         * Single query fetch with all images joined
         * Used only on detail page
         */
        @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.images WHERE l.id = :id")
        Optional<Listing> findByIdWithImages(@Param("id") UUID id);

        List<Listing> findByPromoteTrue();

        List<Listing> findByOnHoldFalse();

        boolean existsByTitle(String title);

        long countByStatusAndStockQuantityGreaterThanAndOnHoldFalse(
                ListingStatus status, int stockQuantity);

        @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Listing l " +
                "WHERE l.id = :listingId " +
                "AND l.stockQuantity > 0 " +
                "AND l.status = :status")
        boolean isAvailableForTrade(
                @Param("listingId") UUID listingId,
                @Param("status") ListingStatus status);

        @Query("SELECT l FROM Listing l " +
                "WHERE l.stockQuantity > 0 " +
                "AND l.status = :status " +
                "AND l.onHold = false")
        Page<Listing> findAllWithStatus(
                @Param("status") ListingStatus status, Pageable pageable);

        @Query("SELECT l FROM Listing l " +
                "WHERE l.stockQuantity > 0 " +
                "AND l.status = :status " +
                "AND l.onHold = false " +
                "AND l.owner.username = :username")
        Page<Listing> findAllWithStatusAndUsername(
                @Param("status") ListingStatus status,
                @Param("username") String username,
                Pageable pageable);

        Page<Listing> findAll(Pageable pageable);

        @Query(value = """
            SELECT l.id FROM listings l
            WHERE l.status = 'AVAILABLE'
            AND l.on_hold = false
            AND l.stock_quantity > 0
            AND (
                to_tsvector('english',
                    coalesce(l.title,'') || ' ' ||
                    coalesce(l.artist_name,'') || ' ' ||
                    coalesce(l.label_name,'') || ' ' ||
                    coalesce(l.format,'')
                ) @@ plainto_tsquery('english', :query)
                OR l.title ILIKE '%' || :query || '%'
                OR l.artist_name ILIKE '%' || :query || '%'
            )
            ORDER BY
                ts_rank(
                    to_tsvector('english',
                        coalesce(l.title,'') || ' ' ||
                        coalesce(l.artist_name,'') || ' ' ||
                        coalesce(l.label_name,'') || ' ' ||
                        coalesce(l.format,'')
                    ),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
        List<UUID> fullTextSearch(
                @Param("query") String query,
                @Param("size") int size,
                @Param("offset") int offset);

        @Query(value = """
            SELECT COUNT(*) FROM listings l
            WHERE l.status = 'AVAILABLE'
            AND l.on_hold = false
            AND l.stock_quantity > 0
            AND (
                to_tsvector('english',
                    coalesce(l.title,'') || ' ' ||
                    coalesce(l.artist_name,'') || ' ' ||
                    coalesce(l.label_name,'') || ' ' ||
                    coalesce(l.format,'')
                ) @@ plainto_tsquery('english', :query)
                OR l.title ILIKE '%' || :query || '%'
                OR l.artist_name ILIKE '%' || :query || '%'
            )
            """, nativeQuery = true)
        long countFullTextSearch(@Param("query") String query);

        @Query(value = """
            SELECT l.id FROM listings l
            WHERE l.status = 'AVAILABLE'
            AND l.on_hold = false
            AND l.stock_quantity > 0
            ORDER BY l.created_at DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
        List<UUID> findAllAvailableIds(
                @Param("size") int size,
                @Param("offset") int offset);


}
