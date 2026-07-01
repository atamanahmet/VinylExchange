package com.atamanahmet.vinylexchange.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class PiiAttributeConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptionService.decrypt(dbData);
    }
}
