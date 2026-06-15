package com.atamanahmet.vinylexchange.dto.payment;

import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeResolveRequest(

        @NotNull(message = "Resolution is required")
        DisputeResolvedEvent.Resolution resolution,

        @NotBlank(message = "Review note cannot be blank")
        @Size(max = 500, message = "Review note cannot exceed 500 characters")
        String reviewNote
) {}