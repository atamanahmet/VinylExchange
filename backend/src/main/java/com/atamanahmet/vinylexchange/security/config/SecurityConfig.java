package com.atamanahmet.vinylexchange.security.config;

import com.atamanahmet.vinylexchange.logging.MdcLoggingFilter;
import com.atamanahmet.vinylexchange.security.filter.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${file.upload-listing-dir}")
    private String uploadListingDir;

    @Value("${file.upload-cms-dir}")
    private String uploadCmsDir;

    private final JWTAuthFilter jwtAuthFilter;
    private final MdcLoggingFilter mdcLoggingFilter;

    // --- public routes, no auth required ---
    private static final String[] PUBLIC_ROUTES = {
            "/",
            "/login",
            "/error",
            "/register",
            "/api/me",
            "/api/mb/search/**",
            "/api/payment/callback",
            "/api/shipment/webhook/**",
            "/api/cms/**",
            "/listing/**",
            "/api/listings/**",
            "/api/listings/search",
            "/api/reference/**",
            "/api/users/by-username/**",
            "/uploads/cms/**",
            "/uploads/listings/**",
            "/demo-covers/**",
            "/actuator/health"
    };

    // --- allowed CORS origins ---
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost",
            "https://sandbox-api.iyzipay.com",
            "https://api.iyzipay.com"
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(mdcLoggingFilter, JWTAuthFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/cart/items/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/newlisting").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()
                        .requestMatchers("/api/messages/conversation/**").authenticated()
                        .anyRequest().authenticated())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Maps static resource url patterns to their locations.
     * Add new entries here when adding new static resource paths.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Map<String, String> resourceMappings = new LinkedHashMap<>();
        resourceMappings.put("/uploads/listings/**", "file:" + uploadListingDir);
        resourceMappings.put("/uploads/cms/**",      "file:" + uploadCmsDir);
        resourceMappings.put("/demo-covers/**",      "classpath:demo/covers/");

        resourceMappings.forEach((pattern, location) ->
                registry.addResourceHandler(pattern)
                        .addResourceLocations(location)
                        .setCachePeriod(3600)
        );
    }
}