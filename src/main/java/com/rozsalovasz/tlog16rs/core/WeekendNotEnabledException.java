package com.rozsalovasz.tlog16rs.core;

public class WeekendNotEnabledException extends RuntimeException {

    public WeekendNotEnabledException() {
    }
    
    public WeekendNotEnabledException(String message) {
        super(message);
    }
    
}
