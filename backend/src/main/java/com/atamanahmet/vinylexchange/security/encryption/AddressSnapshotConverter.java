package com.atamanahmet.vinylexchange.security.encryption;

import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class AddressSnapshotConverter implements AttributeConverter<AddressSnapshot, String> {

    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    /** Serializes and encrypts snapshot for DB storage. */
    @Override
    public String convertToDatabaseColumn(AddressSnapshot attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            return encryptionService.encrypt(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize address snapshot", e);
        }
    }

    /** Decrypts and deserializes snapshot from DB column. */
    @Override
    public AddressSnapshot convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            String json = encryptionService.decrypt(dbData);
            return objectMapper.readValue(json, AddressSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize address snapshot", e);
        }
    }
}
