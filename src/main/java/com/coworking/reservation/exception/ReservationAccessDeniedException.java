package com.coworking.reservation.exception;

public class ReservationAccessDeniedException extends RuntimeException{

    public ReservationAccessDeniedException(String message) {
        super(message);
    }
}
