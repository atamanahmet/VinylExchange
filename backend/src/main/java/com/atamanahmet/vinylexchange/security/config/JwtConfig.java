package com.atamanahmet.vinylexchange.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class JwtConfig {
    @Value("${jwt.secret}")
    private String SECRET;
    @Value("${jwt.expiration}")
    private long expiration;

    // public static final String BEARER = "Bearer ";
    // public static final String AUTHORIZATION = "Authorization";
}
