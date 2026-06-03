package com.atamanahmet.vinylexchange.config.json;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PriceTlSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        BigDecimal tl = BigDecimal.valueOf(value).movePointLeft(2); // kurus to tl

        jsonGenerator.writeNumber(tl);

    }
}
