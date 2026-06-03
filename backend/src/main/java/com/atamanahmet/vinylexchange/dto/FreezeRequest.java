package com.atamanahmet.vinylexchange.dto;

import jakarta.validation.constraints.NotNull;

public record FreezeRequest(@NotNull Boolean action) {
}
