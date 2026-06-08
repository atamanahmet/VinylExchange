package com.atamanahmet.vinylexchange.domain.entity;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Snapshot, listing may be deleted after purchase */
    private UUID listingId;

    private String listingTitle;

    private String listingMainImageUrl;

    /** Snapshot, seller at time of purchase */
    private UUID sellerId;

    @JsonSerialize(using = PriceTlSerializer.class)
    private Long unitPrice;

    @JsonSerialize(using = PriceTlSerializer.class)
    private Long subTotal;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}