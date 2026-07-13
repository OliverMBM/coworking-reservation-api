package com.coworking.reservation.config;

import com.coworking.reservation.security.JwtAuthenticationEntryPoint;
import com.coworking.reservation.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(authenticationEntryPoint)
        );

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                .requestMatchers(
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll()

                .requestMatchers("/actuator/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/spaces")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT, "/api/spaces/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/spaces/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/spaces/**")
                .hasAnyRole("USER", "ADMIN")

                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                .requestMatchers("/api/reports/**")
                .hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/reservations")
                .hasRole("USER")

                .requestMatchers(HttpMethod.GET, "/api/reservations/mine")
                .hasRole("USER")

                .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/cancel")
                .hasRole("USER")

                .anyRequest()
                .authenticated()
        );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
