package com.atamanahmet.vinylexchange.config.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DiscountDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        String raw = parser.getValueAsString();

        if (raw == null || raw.isBlank())

            return 0;

        BigDecimal percent = new BigDecimal(raw.trim());

        return percent
                .movePointRight(2) // to basis point
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

    }
}
