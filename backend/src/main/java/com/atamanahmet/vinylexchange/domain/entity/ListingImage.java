package com.atamanahmet.vinylexchange.domain.entity;

import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listing_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Cloudinary public_id or local relative path, used for delete */
    private String publicId;

    /** url served to frontend */
    private String secureUrl;

    /** display order */
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageProvider provider;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
}