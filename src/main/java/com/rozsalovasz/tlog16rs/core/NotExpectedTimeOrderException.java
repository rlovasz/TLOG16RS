package com.rozsalovasz.tlog16rs.core;

public class NotExpectedTimeOrderException extends RuntimeException {

    public NotExpectedTimeOrderException() {
    }
    
    public NotExpectedTimeOrderException(String message) {
        super(message);
    }
    
}
