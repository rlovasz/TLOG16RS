package com.rozsalovasz.tlog16rs.core;

public class InvalidTaskIdException extends Exception {

    public InvalidTaskIdException() {
    }
    
    public InvalidTaskIdException(String message) {
        super(message);
    }
    
}
