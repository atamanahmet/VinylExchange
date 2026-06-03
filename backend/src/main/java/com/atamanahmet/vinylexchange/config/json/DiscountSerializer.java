package com.atamanahmet.vinylexchange.config.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DiscountSerializer extends JsonSerializer<Integer> {

    @Override
    public void serialize(Integer value, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {

        if (value == null || value == 0) {
            jsonGenerator.writeNumber(BigDecimal.ZERO);
            return;
        }

        BigDecimal percent = BigDecimal
                .valueOf(value)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        jsonGenerator.writeNumber(percent);

    }
}
