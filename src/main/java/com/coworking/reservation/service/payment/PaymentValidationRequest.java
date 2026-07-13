package com.coworking.reservation.service.payment;

import java.math.BigDecimal;

public record PaymentValidationRequest(
        Long reservationId,
        BigDecimal amount,
        String paymentMethod
) {
}
