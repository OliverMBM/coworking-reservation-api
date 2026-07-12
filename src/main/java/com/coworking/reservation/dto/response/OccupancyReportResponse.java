package com.coworking.reservation.dto.response;

import com.coworking.reservation.entity.enums.SpaceType;

import java.math.BigDecimal;

public record OccupancyReportResponse(
        Long spaceId,
        String spaceName,
        SpaceType spaceType,
        BigDecimal occupancyPercentage
) {
}
