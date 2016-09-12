package com.rozsalovasz.tlog16rs.core;

public class NegativeMinutesOfWorkException extends Exception {

    public NegativeMinutesOfWorkException() {
    }
    
    public NegativeMinutesOfWorkException(String message) {
        super(message);
    }
}