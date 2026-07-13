package com.coworking.reservation.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private static final String PAYMENT_CIRCUIT_BREAKER = "paymentService";

    private final RestClient paymentRestClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public PaymentValidationResponse validate(PaymentValidationRequest request){
        return circuitBreakerFactory
                .create(PAYMENT_CIRCUIT_BREAKER)
                .run(
                        () -> callPaymentService(request),
                        throwable -> fallback(request, throwable)
                );
    }

    private PaymentValidationResponse callPaymentService(
            PaymentValidationRequest request
    ){
        PaymentValidationResponse response = paymentRestClient.post()
                .uri("/payments/validate")
                .body(request)
                .retrieve()
                .body(PaymentValidationResponse.class);

        if (response == null){
            throw new IllegalStateException("Pago vacio - respuesta del servicio");
        }

        return response;
    }

    private PaymentValidationResponse fallback(
            PaymentValidationRequest request,
            Throwable throwable
    ){
        return PaymentValidationResponse.pending(
                "Servicio de pago no disponible. La reservacion queda en 'pendiente'."
        );
    }
}
