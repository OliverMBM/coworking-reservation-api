package com.coworking.reservation.auth;

import com.coworking.reservation.AbstractIntegrationTest;
import com.coworking.reservation.dto.request.LoginRequest;
import com.coworking.reservation.dto.request.RegisterRequest;
import com.coworking.reservation.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerShouldReturnJwtToken() {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test.user@email.com",
                "Password123!"
        );

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    void loginShouldReturnJwtToken() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Login User",
                "login.user@email.com",
                "Password123!"
        );

        restTemplate.postForEntity(
                "/api/auth/register",
                registerRequest,
                AuthResponse.class
        );

        LoginRequest loginRequest = new LoginRequest(
                "login.user@email.com",
                "Password123!"
        );

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
    }
}
