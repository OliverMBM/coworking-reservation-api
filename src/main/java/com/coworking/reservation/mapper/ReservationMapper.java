package com.coworking.reservation.mapper;

import com.coworking.reservation.dto.response.ReservationResponse;
import com.coworking.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getSpace().getId(),
                reservation.getSpace().getName(),
                reservation.getSpace().getType(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getTotalPrice(),
                reservation.getStatus(),
                reservation.getPaymentReference(),
                reservation.getCreatedAt()
        );
    }
}
