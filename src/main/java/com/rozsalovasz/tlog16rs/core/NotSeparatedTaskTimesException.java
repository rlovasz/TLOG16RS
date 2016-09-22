package com.rozsalovasz.tlog16rs.core;

public class NotSeparatedTaskTimesException extends RuntimeException {

    public NotSeparatedTaskTimesException() {
    }
    
    public NotSeparatedTaskTimesException(String message) {
        super(message);
    }
}
