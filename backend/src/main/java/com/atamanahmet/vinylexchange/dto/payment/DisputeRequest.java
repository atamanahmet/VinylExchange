package com.atamanahmet.vinylexchange.dto.payment;

import com.atamanahmet.vinylexchange.domain.enums.DisputeReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeRequest(

        @NotNull(message = "Dispute reason is required")
        DisputeReason reason,

        @NotBlank(message = "Note cannot be blank")
        @Size(max = 500, message = "Note cannot exceed 500 characters")
        String note
) {}