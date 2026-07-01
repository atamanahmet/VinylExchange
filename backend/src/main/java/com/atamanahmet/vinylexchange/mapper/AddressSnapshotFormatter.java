package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AddressSnapshotFormatter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AddressSnapshotFormatter() {}

    public static String toSummary(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        try {
            AddressSnapshot snapshot = MAPPER.readValue(snapshotJson, AddressSnapshot.class);
            return format(snapshot);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String format(AddressSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, snapshot.fullName());
        appendPart(builder, snapshot.addressLine());
        appendPart(builder, snapshot.district());
        appendPart(builder, snapshot.city());
        appendPart(builder, snapshot.postalCode());
        appendPart(builder, snapshot.country());
        return builder.isEmpty() ? null : builder.toString();
    }

    private static void appendPart(StringBuilder builder, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(part.trim());
    }
}
