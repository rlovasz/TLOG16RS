package com.rozsalovasz.tlog16rs.core;

public class NotTheSameMonthException extends RuntimeException {

    public NotTheSameMonthException() {
    }
    
    public NotTheSameMonthException(String message) {
        super(message);
    }
    
}
