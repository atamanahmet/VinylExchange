package com.atamanahmet.vinylexchange.dto.user;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradePreferenceDTO {
    private UUID id;
    private String desiredItem;
    private Double extraAmount;
    private PaymentDirection paymentDirection;

    public TradePreferenceDTO(TradePreference tradePreference) {
        this.id = tradePreference.getId();
        this.desiredItem = tradePreference.getDesiredItem();
        this.extraAmount = tradePreference.getExtraAmount();
        this.paymentDirection = tradePreference.getPaymentDirection();
    }

    public static List<TradePreferenceDTO> fromEntities(List<TradePreference> tradePreferences) {
        if (tradePreferences == null || tradePreferences.isEmpty()) {
            return List.of();
        }

        return tradePreferences.stream()
                .map(TradePreferenceDTO::new)
                .toList();
    }
}
