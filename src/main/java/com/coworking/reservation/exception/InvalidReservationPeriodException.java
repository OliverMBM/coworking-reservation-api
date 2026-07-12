package com.coworking.reservation.exception;

public class InvalidReservationPeriodException extends RuntimeException{

    public InvalidReservationPeriodException(String message) {
        super(message);
    }
}
