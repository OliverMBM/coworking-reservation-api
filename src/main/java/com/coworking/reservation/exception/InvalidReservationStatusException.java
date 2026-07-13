package com.coworking.reservation.exception;

public class InvalidReservationStatusException extends RuntimeException{

    public InvalidReservationStatusException(String message){
        super(message);
    }
}
