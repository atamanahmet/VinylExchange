package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.atamanahmet.vinylexchange.domain.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Listing> listings;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishlistItem> wishlist = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    /** IBAN for payout when order completes */
    private String ibanForPayout;

    /** Legal name for payout records */
    private String legalName;

    /** Set to true after admin verifies payout info */
    @Builder.Default
    @Column(nullable = false)
    private boolean payoutInfoVerified = false;

    private void updateStatusTimeStamps(UserStatus userStatus) {

        switch (userStatus) {

            case ACTIVE:
                this.activatedAt = LocalDateTime.now();
                break;
            case INACTIVE:
                this.deactivatedAt = LocalDateTime.now();
                break;
            case DELETED:
                this.deletedAt = LocalDateTime.now();
                break;
            case SUSPENDED:
                this.suspendedAt = LocalDateTime.now();
                break;
            case BANNED:
                this.bannedAt = LocalDateTime.now();
                break;

            default:
                break;
        }
    }

    public void setStatus(UserStatus newStatus) {

        this.status = newStatus;
        updateStatusTimeStamps(newStatus);
    }
}
