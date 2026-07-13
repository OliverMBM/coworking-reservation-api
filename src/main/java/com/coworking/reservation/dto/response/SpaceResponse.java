package com.coworking.reservation.dto.response;

import com.coworking.reservation.entity.enums.SpaceType;

import java.math.BigDecimal;

public record SpaceResponse(
        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        BigDecimal hourlyRate,
        Boolean active
) {
}
