package com.coworking.reservation.dto.request;

import com.coworking.reservation.entity.enums.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSpaceRequest(
        @NotBlank(message = "El nombre del espacio es requerido")
        String name,

        @NotNull(message = "El tipo de espacio es requerido")
        SpaceType type,

        @NotNull(message = "Debe ingresar la capacidad")
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        Integer capacity,

        @NotBlank(message = "La locación es requerida")
        String location,

        @NotNull(message = "El cargo por hora es requerido")
        @DecimalMin(value = "0.01", message = "El cargo por hora debe ser mayor a cero")
        BigDecimal hourlyRate
) {
}
