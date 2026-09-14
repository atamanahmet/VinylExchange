package com.atamanahmet.vinylexchange.service.user;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.exception.InvalidCurrentPasswordException;
import com.atamanahmet.vinylexchange.exception.UserNotFoundException;
import com.atamanahmet.vinylexchange.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Updates password and passwordChangedAt. Returns destination email for notification.
     * Caller sends email after this method returns so mail failure cannot roll back the change.
     */
    @Transactional
    public String changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        return user.getEmail();
    }
}
