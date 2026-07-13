package com.coworking.reservation.dto.response;

import com.coworking.reservation.entity.enums.ReservationStatus;
import com.coworking.reservation.entity.enums.SpaceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        String userName,
        Long spaceId,
        String spaceName,
        SpaceType spaceType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal totalPrice,
        ReservationStatus status,
        String paymentReference,
        LocalDateTime createdAt
) {
}
