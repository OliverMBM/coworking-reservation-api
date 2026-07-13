package com.coworking.reservation.service.payment;

public record PaymentValidationResponse(
        boolean approved,
        String reference,
        String message
) {

    public static PaymentValidationResponse approved(String reference){
        return new PaymentValidationResponse(
                true,
                reference,
                "Pago aprovado"
        );
    }

    public static PaymentValidationResponse pending(String message){
        return new PaymentValidationResponse(
                false,
                null,
                message
        );
    }
}
