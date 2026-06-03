package com.atamanahmet.vinylexchange.config.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PriceKurusDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        String raw = parser.getValueAsString();

        if (raw == null || raw.isEmpty()) {
            return 0L;
        }

        BigDecimal tl = new BigDecimal(raw.trim());

        return tl.movePointRight(2) // tl to kurus
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

    }
}
