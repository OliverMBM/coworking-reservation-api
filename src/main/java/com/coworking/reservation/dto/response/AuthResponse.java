package com.coworking.reservation.dto.response;

public record AuthResponse(
        String tokenType,
        String accessToken
) {
}
