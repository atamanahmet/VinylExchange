package com.atamanahmet.vinylexchange.dto.listing;

import jakarta.validation.constraints.NotNull;

public record PromoteRequest(@NotNull Boolean action) {

}
