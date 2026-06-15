package com.atamanahmet.vinylexchange.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRequest(

        @NotBlank(message = "Cancel reason cannot be blank")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {}