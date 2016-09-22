package com.rozsalovasz.tlog16rs.core;

public class NotNewMonthException extends RuntimeException {

    public NotNewMonthException() {
    }
    
    public NotNewMonthException(String message) {
         super(message);
    }
    
}
