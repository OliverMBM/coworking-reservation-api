package com.coworking.reservation.space;

import com.coworking.reservation.AbstractIntegrationTest;
import com.coworking.reservation.dto.request.CreateSpaceRequest;
import com.coworking.reservation.dto.request.RegisterRequest;
import com.coworking.reservation.dto.response.AuthResponse;
import com.coworking.reservation.entity.enums.SpaceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SpaceSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void userShouldNotCreateSpace() {
        String token = registerUserAndGetToken();

        CreateSpaceRequest request = new CreateSpaceRequest(
                "Meeting Room A",
                SpaceType.MEETING_ROOM,
                8,
                "Second floor",
                BigDecimal.valueOf(25)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/spaces",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String registerUserAndGetToken() {
        RegisterRequest request = new RegisterRequest(
                "Normal User",
                "normal.user@email.com",
                "Password123!"
        );

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                AuthResponse.class
        );

        assertThat(response.getBody()).isNotNull();

        return response.getBody().accessToken();
    }
}
