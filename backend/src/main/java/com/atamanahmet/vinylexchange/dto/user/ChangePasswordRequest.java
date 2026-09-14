package com.atamanahmet.vinylexchange.dto.user;

import com.atamanahmet.vinylexchange.validation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password must not be blank") String currentPassword,

        @NotBlank(message = "Password must not be blank")
        @Size(message = "Password must be minimum 8 maximum 64 character", min = 8, max = 64)
        @StrongPassword String newPassword) {
}
