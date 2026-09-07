package com.atamanahmet.vinylexchange.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks Cloudinary public IDs orphaned when listing or listing image rows are deleted, no FK to those tables.
 */
@Entity
@Table(name = "orphaned_cloud_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrphanedCloudAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
