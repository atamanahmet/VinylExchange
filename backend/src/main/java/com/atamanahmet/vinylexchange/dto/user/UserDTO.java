package com.atamanahmet.vinylexchange.dto.user;

import com.atamanahmet.vinylexchange.domain.entity.User;

import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;

public record UserDTO(
                String username,
                String email,
                String publicId) {

        public UserDTO(User user) {
                this(user.getUsername(), user.getEmail(), user.getPublicId());
        }

        public UserDTO(UserDetailsImpl userPrincipal) {
                this(userPrincipal.getUsername(), userPrincipal.getEmail(), userPrincipal.getPublicId());
        }

}
