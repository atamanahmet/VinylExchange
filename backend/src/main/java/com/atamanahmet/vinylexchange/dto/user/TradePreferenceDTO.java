package com.atamanahmet.vinylexchange.dto.user;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;

public record TradePreferenceDTO(
        UUID id,
        String desiredItem,
        Double extraAmount,
        PaymentDirection paymentDirection
) {
    public TradePreferenceDTO(TradePreference tradePreference) {
        this(
                tradePreference.getId(),
                tradePreference.getDesiredItem(),
                tradePreference.getExtraAmount(),
                tradePreference.getPaymentDirection()
        );
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
