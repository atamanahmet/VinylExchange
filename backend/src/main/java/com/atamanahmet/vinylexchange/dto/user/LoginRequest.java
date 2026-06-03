package com.atamanahmet.vinylexchange.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username must not be blank") @Size(message = "Username must be minimum 5, maximum 50 character", min = 5, max = 50) String username,

        @NotBlank(message = "Password must not be blank") @Size(message = "Password must be minimum 8, maximum 64 character", min = 8, max = 64) String password) {

    public LoginRequest {
        username = username != null ? username.trim() : null;
    }
}