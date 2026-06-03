package com.atamanahmet.vinylexchange.security.config;

import java.util.List;

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

import com.atamanahmet.vinylexchange.security.filter.JWTAuthFilter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${file.upload-listing-dir}")
    private String UPLOAD_LISTING_DIR;

    @Value("${file.upload-placeholder-dir}")
    private String UPLOAD_PLACEHOLDER_DIR;

    @Value("${file.upload-cms-dir}")
    private String UPLOAD_CMS_DIR;

    private final JWTAuthFilter jwtAuthFilter;

    public SecurityConfig(JWTAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/",
                                "/login",
                                "/error",
                                "/api/me",
                                "/register",
                                "/api/mb/search/**",
                                "/api/cms/**",
                                "/listing/**",
                                "/api/listings/**",
                                "/api/listings/search",
                                "/uploads/cms/**",
                                "/uploads/listings/**",
                                "/uploads/placeholders/**",
                                "/actuator/health")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/cart/items/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/newlisting").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()
                        .requestMatchers("/api/messages/conversation/**").authenticated()
                        .anyRequest()
                        .authenticated())
                .logout(logout -> logout.disable());
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173","http://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/uploads/listings/**","/uploads/placeholders/**", "/uploads/cms/**")
                .addResourceLocations("file:" + UPLOAD_LISTING_DIR, "file:"+UPLOAD_PLACEHOLDER_DIR, "file:" + UPLOAD_CMS_DIR)
                .setCachePeriod(3600); // 1 hour
    }

}
