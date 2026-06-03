package com.atamanahmet.vinylexchange.security.util;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.security.config.JwtConfig;
import com.atamanahmet.vinylexchange.domain.entity.User;

@Component
public class JwtTokenUtil {

    private final JwtConfig jwtConfig;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    public JwtTokenUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtConfig.getExpiration());

        String token = JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("username", user.getUsername())
                .withClaim("roles", user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiry))
                .sign(Algorithm.HMAC512(jwtConfig.getSECRET()));

        return token;
    }

    public String generateToken(UUID id, String username, Set<RoleName> roles) {

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtConfig.getExpiration());

        String token = JWT.create()
                .withSubject(id.toString())
                .withClaim("roles", roles.stream()
                        .map(role -> role.name())
                        .collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiry))
                .sign(Algorithm.HMAC512(jwtConfig.getSECRET()));

        return token;
    }

    public boolean validateToken(String token) {

        try {
            JWT.require(Algorithm.HMAC512(jwtConfig.getSECRET()))
                    .build()
                    .verify(token);

            return true;

        } catch (JWTVerificationException e) {

            logger.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // xtracting id from token
    public UUID extractUserId(String token) {

        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(jwtConfig.getSECRET()))
                    .build()
                    .verify(token);

            String idStr = decodedJWT.getSubject();

            return UUID.fromString(idStr);

        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Invalid or expired token");
        }
    }
}
