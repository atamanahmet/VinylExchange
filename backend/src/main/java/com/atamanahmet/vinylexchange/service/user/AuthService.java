package com.atamanahmet.vinylexchange.service.user;

import java.util.Set;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.user.AuthResponse;
import com.atamanahmet.vinylexchange.dto.user.LoginRequest;
import com.atamanahmet.vinylexchange.dto.user.RegisterRequest;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;

import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;
import com.atamanahmet.vinylexchange.security.util.JwtTokenUtil;

import com.atamanahmet.vinylexchange.exception.NoCurrentUserException;
import com.atamanahmet.vinylexchange.exception.RegistrationValidationException;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.repository.user.UserRepository;
import com.atamanahmet.vinylexchange.domain.enums.UserStatus;
import com.atamanahmet.vinylexchange.dto.user.UserDTO;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RoleService roleService;

    public AuthResponse authenticateUser(LoginRequest loginRequestDTO) {
        if (loginRequestDTO == null)
            throw new BadCredentialsException("Invalid credentials");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.username(), loginRequestDTO.password()));

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String token = jwtTokenUtil.generateToken(
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getRoles());

        return new AuthResponse(
                new UserDTO(userPrincipal.getUsername(), userPrincipal.getEmail()), token);
    }

    public AuthResponse registerUser(RegisterRequest registerRequest, Role role) {

        validateRegistration(registerRequest);

        User user = new User();

        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setStatus(UserStatus.PENDING);

        if (role == null) {
            role = roleService.getRoleByName(RoleName.ROLE_USER);
        }

        user.setRoles(Set.of(role));

        System.out.println(user.getRoles());

        User savedUser = userRepository.save(user);

        String token = jwtTokenUtil.generateToken(savedUser);

        return new AuthResponse(
                new UserDTO(savedUser.getUsername(), savedUser.getEmail()), token);
    }

    public void validateRegistration(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.username())
                || userRepository.existsByEmail(registerRequest.email())) {

            throw new RegistrationValidationException("Registration failed: invalid username or email");
        }
    }

    public User validateLogin(LoginRequest loginRequestDTO) {

        User user = userRepository
                .findByUsername(loginRequestDTO.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return user;
    }

    public UserDTO getUserDTO(String token) {

        UUID userId = jwtTokenUtil.extractUserId(token);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NoCurrentUserException());

        return new UserDTO(user);
    }

    public User getUserFromToken(String token) {

        UUID userId = jwtTokenUtil.extractUserId(token);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new NoCurrentUserException());

        return user;
    }

    public Set<Role> giveUserAdminRole(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new NoCurrentUserException());

        if (user != null) {
            Role adminRole = roleService.getRoleByName(RoleName.ROLE_ADMIN);
            user.getRoles().add(adminRole);
            userRepository.save(user);
            return user.getRoles();

        }
        return null;

    }
}
