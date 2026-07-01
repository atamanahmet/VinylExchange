package com.atamanahmet.vinylexchange.domain.entity;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.AddressType;
import com.atamanahmet.vinylexchange.security.encryption.PiiAttributeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_addresses",
        indexes = @Index(name = "idx_user_address_user_id", columnList = "userId"))
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String label;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String fullName;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String phone;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String city;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String postalCode;

    @Builder.Default
    @Column(nullable = false)
    private String country = "TR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDefault = false;
}
