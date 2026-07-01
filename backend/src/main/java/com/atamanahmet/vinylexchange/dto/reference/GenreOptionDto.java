package com.atamanahmet.vinylexchange.dto.reference;

public record GenreOptionDto(
        Long id,
        String label,
        Long parentId,
        boolean localFlavor) {
}
