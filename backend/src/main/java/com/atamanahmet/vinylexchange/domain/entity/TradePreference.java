package com.atamanahmet.vinylexchange.domain.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trade_prefs")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String desiredItem;

    // positive or negative value delta
    private double extraAmount;

    @NotNull(message = "Payment direction is required")
    @Enumerated(EnumType.STRING)
    private PaymentDirection paymentDirection = PaymentDirection.NO_EXTRA;

    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    @JsonIgnore
    private Listing listing;

}
