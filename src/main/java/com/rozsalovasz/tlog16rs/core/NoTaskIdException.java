package com.rozsalovasz.tlog16rs.core;

public class NoTaskIdException extends RuntimeException {

    public NoTaskIdException() {
    }
    
    public NoTaskIdException(String message) {
        super(message);
    }
    
}
