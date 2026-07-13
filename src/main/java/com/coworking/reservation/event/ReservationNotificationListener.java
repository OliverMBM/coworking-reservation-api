package com.coworking.reservation.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ReservationNotificationListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReservationConfirmedEvent event){
        log.info(
                "Correo de confirmacion simulado para la reservacion {}",
                event.reservationId()
        );
    }
}
