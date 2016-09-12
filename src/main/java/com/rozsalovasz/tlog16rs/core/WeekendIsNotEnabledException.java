package com.rozsalovasz.tlog16rs.core;


public class WeekendIsNotEnabledException extends Exception {

    public WeekendIsNotEnabledException() {
    }
    
    public WeekendIsNotEnabledException(String message) {
        super(message);
    }
    
}