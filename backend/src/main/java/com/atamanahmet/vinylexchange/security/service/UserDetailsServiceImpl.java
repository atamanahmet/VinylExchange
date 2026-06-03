package com.atamanahmet.vinylexchange.security.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;
import com.atamanahmet.vinylexchange.exception.NoCurrentUserException;
import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.repository.user.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoCurrentUserException());

        return new UserDetailsImpl(user);
    }

    public UserDetailsImpl loadUserByUserId(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoCurrentUserException());

        return new UserDetailsImpl(user);
    }
}
