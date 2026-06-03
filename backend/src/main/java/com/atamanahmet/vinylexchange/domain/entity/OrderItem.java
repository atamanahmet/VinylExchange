package com.atamanahmet.vinylexchange.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.enums.OrderItemStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    // when listing removed snapshot can hold basic info
    private UUID listingId;

    private String listingTitle;

    private String listingMainImageUrl;

    @JsonSerialize(using = PriceTlSerializer.class)
    private Long unitPrice;

    @JsonSerialize(using = PriceTlSerializer.class)
    private Long subTotal;

    private Integer quantity;

    private UUID sellerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime shippingDeadline;

}
