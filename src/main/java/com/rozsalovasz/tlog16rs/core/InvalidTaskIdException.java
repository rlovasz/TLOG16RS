package com.rozsalovasz.tlog16rs.core;

public class InvalidTaskIdException extends RuntimeException {

    public InvalidTaskIdException() {
    }
    
    public InvalidTaskIdException(String message) {
        super(message);
    }
}
