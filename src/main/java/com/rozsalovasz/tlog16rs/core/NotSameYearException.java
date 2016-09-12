package com.rozsalovasz.tlog16rs.core;

public class NotSameYearException extends Exception {

    public NotSameYearException() {
    }
    
    public NotSameYearException(String message) {
        super(message);
    }
}
