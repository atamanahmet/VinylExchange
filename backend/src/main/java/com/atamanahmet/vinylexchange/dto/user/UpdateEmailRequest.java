package com.atamanahmet.vinylexchange.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEmailRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email) {

    public UpdateEmailRequest {
        email = email != null ? email.trim().toLowerCase() : null;
    }
}
