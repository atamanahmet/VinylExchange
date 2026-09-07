package com.atamanahmet.vinylexchange.dto.listing;

import jakarta.validation.constraints.NotBlank;

public record FavoriteRequest(@NotBlank String publicId) {

}
