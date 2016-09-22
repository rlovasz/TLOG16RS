package com.rozsalovasz.tlog16rs.core;

public class NotNewDateException extends RuntimeException {

    public NotNewDateException() {
    }
    
    public NotNewDateException(String message) {
        super(message);
    }
    
}
