package com.finance.manager.config;

import com.finance.manager.dto.response.ResponseDtos.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security config using Spring Security 6.x (Spring Boot 3.x).
 *
 * Why session-based instead of JWT?
 * The assignment explicitly requires session-based auth with secure cookies.
 * JWT would also work technically but doesn't follow the spec.
 *
 * Why BCrypt?
 * Industry standard for password hashing — adaptive cost factor,
 * built-in salt, resistant to rainbow table attacks.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for REST API (stateless clients like curl/test scripts)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints: register and login don't need authentication
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // H2 console for local debugging
                .requestMatchers("/h2-console/**").permitAll()
                // All other endpoints require valid session
                .anyRequest().authenticated()
            )
            // Return 401 JSON instead of Spring's default redirect to /login page
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getWriter(),
                        new ErrorResponse("Authentication required", 401));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getWriter(),
                        new ErrorResponse("Access denied", 403));
                })
            )
            // Allow H2 console frames (dev only)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * BCryptPasswordEncoder: industry-standard password hashing.
     * Never store plain text passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}