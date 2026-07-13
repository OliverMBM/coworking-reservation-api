package com.coworking.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull(message = "El id del espacio es requerido")
        Long spaceId,

        @NotNull(message = "Tiempo de inico es requerido")
        @FutureOrPresent(message = "Ingrese un tiempo de incio válido")
        LocalDateTime startTime,

        @NotNull(message = "Tiempo de finalización es requerido")
        @Future(message = "Ingrese un tiempo de finalización válido")
        LocalDateTime endTime,

        @NotBlank(message = "Método de pago es requerido")
        String paymentMethod
) {
}
