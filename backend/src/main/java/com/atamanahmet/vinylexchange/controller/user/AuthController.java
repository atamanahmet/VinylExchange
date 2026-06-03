package com.atamanahmet.vinylexchange.controller.user;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.service.user.AuthService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.atamanahmet.vinylexchange.dto.user.AuthResponse;
import com.atamanahmet.vinylexchange.dto.user.LoginRequest;
import com.atamanahmet.vinylexchange.dto.user.RegisterRequest;
import com.atamanahmet.vinylexchange.security.util.JwtCookieUtil;

import com.atamanahmet.vinylexchange.dto.user.UserDTO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieUtil jwtCookieUtil;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response) {

        AuthResponse authResponseDTO = authService.registerUser(registerRequest, null);

        Cookie cookie = jwtCookieUtil.createJwtCookie(authResponseDTO.token());

        response.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        AuthResponse authResponseDTO = authService.authenticateUser(loginRequest);

        Cookie cookie = jwtCookieUtil.createJwtCookie(authResponseDTO.token());

        response.addCookie(cookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authResponseDTO.userDTO());
    }

    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logOut(HttpServletResponse response) {

        jwtCookieUtil.revokeJwtCookie(response);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/api/me")
    public ResponseEntity<UserDTO> getCurrentUser() {

        User currentUser = UserUtil.getCurrentUser();

        UserDTO userDTO = new UserDTO(currentUser);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDTO);
    }

}
