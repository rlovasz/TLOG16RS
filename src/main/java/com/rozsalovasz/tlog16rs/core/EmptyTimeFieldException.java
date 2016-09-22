package com.rozsalovasz.tlog16rs.core;

public class EmptyTimeFieldException extends RuntimeException {

    public EmptyTimeFieldException() {
    } 
    
    public EmptyTimeFieldException(String message) {
        super(message);
    }
    
}
