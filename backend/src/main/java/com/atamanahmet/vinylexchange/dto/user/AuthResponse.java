package com.atamanahmet.vinylexchange.dto.user;

public record AuthResponse(
        UserDTO userDTO,
        String token) {
}
